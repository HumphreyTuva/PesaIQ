# SMS Parser Engine

The `MpesaParser` is the core component of PesaIQ. It uses a series of optimized Regular Expressions (Regex) to extract structured data from semi-structured M-Pesa SMS notifications.

## Supported Transaction Types

The parser identifies and extracts details for the following:

- **Send Money:** Detects recipient name, phone number, amount, and transaction ID.
- **Receive Money:** Handles various "received from" and "deposited by" formats.
- **Paybill:** Extracts the business name or account number and amount.
- **Buy Goods:** Detects till payments and merchant names.
- **Withdrawals:** Identifies agent withdrawals and amounts.
- **Airtime:** Tracks airtime purchases for self or other numbers.

## Parsing Logic

1. **Validation:** Checks if the sender ID contains "MPESA" or if the body contains keywords like "Ksh" and "Confirmed".
2. **Regex Matching:** Sequentially attempts to match the body against specific patterns for each transaction type.
3. **Data Extraction:** Groups within the regex capture specific fields (Amount, ID, Recipient).
4. **Normalization:** Removes commas from currency strings and converts dates to system timestamps.
5. **Categorization:** Applies a rule-based engine to assign a category based on the recipient name (e.g., "KPLC" -> "Utilities").

## Example Regex (Send Money)
```regex
(?:([A-Z0-9]{8,12})\s+)?Confirmed\.\s*Ksh([\d,]+\.?\d*)\s+sent to\s+(.+?)\s+(\d{9,12})\s+on\s+([\d/]+)\s+at\s+([\d:]+ [AP]M)
```

## Smart Categorization
The parser includes a `categorizeBusiness` function that uses keyword mapping to automatically tag expenses:
- **Groceries:** Naivas, Quickmart, Carrefour, etc.
- **Utilities:** KPLC, Kenya Power.
- **Transport:** Uber, Bolt, Little.
- **Food:** KFC, Java, Art Caffe.
