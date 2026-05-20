"""
setup.py — Run this ONCE to download the missing gradle-wrapper.jar
Works on Windows, Mac, and Linux.

Usage:
  1. Put this file inside the mpesa-tracker folder (same level as build.gradle)
  2. Run:  python setup.py   OR   python3 setup.py
  3. Then open the project in Android Studio and sync normally
"""

import urllib.request
import zipfile
import os
import shutil
import sys

GRADLE_VERSION  = "8.4"
GRADLE_ZIP_URL  = f"https://services.gradle.org/distributions/gradle-{GRADLE_VERSION}-bin.zip"
WRAPPER_DIR     = os.path.join("gradle", "wrapper")
WRAPPER_JAR     = os.path.join(WRAPPER_DIR, "gradle-wrapper.jar")

def download_with_progress(url, dest):
    print(f"Downloading Gradle {GRADLE_VERSION} (~130 MB)...")
    def reporthook(count, block_size, total_size):
        if total_size > 0:
            percent = int(count * block_size * 100 / total_size)
            sys.stdout.write(f"\r  Progress: {min(percent, 100)}%")
            sys.stdout.flush()
    urllib.request.urlretrieve(url, dest, reporthook)
    print("\n  Download complete.")

def main():
    # Check we're in the right folder
    if not os.path.exists("build.gradle"):
        print("❌  ERROR: Run this script from inside the mpesa-tracker folder.")
        print("    (The folder that contains build.gradle)")
        sys.exit(1)

    if os.path.exists(WRAPPER_JAR) and os.path.getsize(WRAPPER_JAR) > 10_000:
        print("✅  gradle-wrapper.jar already exists. Nothing to do!")
        print("    Open the project in Android Studio and sync.")
        return

    os.makedirs(WRAPPER_DIR, exist_ok=True)

    zip_path = "gradle-temp.zip"
    try:
        download_with_progress(GRADLE_ZIP_URL, zip_path)

        print("Extracting gradle-wrapper.jar ...")
        with zipfile.ZipFile(zip_path, "r") as z:
            # The jar is at gradle-X.X/lib/gradle-wrapper-X.X.jar inside the zip
            matches = [n for n in z.namelist()
                       if "gradle-wrapper" in n and n.endswith(".jar")]
            if not matches:
                # Fallback: look for any wrapper jar
                matches = [n for n in z.namelist()
                           if "wrapper" in n.lower() and n.endswith(".jar")]

            if not matches:
                print("❌  Could not find gradle-wrapper.jar inside the zip.")
                print("    Try manually downloading from:")
                print("    https://services.gradle.org/distributions/gradle-8.4-bin.zip")
                sys.exit(1)

            jar_name = matches[0]
            print(f"   Found: {jar_name}")
            with z.open(jar_name) as src, open(WRAPPER_JAR, "wb") as dst:
                shutil.copyfileobj(src, dst)

        size_kb = os.path.getsize(WRAPPER_JAR) // 1024
        print(f"\n✅  Done! gradle-wrapper.jar saved ({size_kb} KB)")
        print("\nNext steps:")
        print("  1. Open mpesa-tracker in Android Studio")
        print("  2. Click 'Sync Now' when prompted")
        print("  3. Run the app on your device or emulator")

    finally:
        if os.path.exists(zip_path):
            os.remove(zip_path)

if __name__ == "__main__":
    main()
