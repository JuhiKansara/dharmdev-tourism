# Dharmdev Tourism and Travels — Billing/Estimate System

## 1. Overview
A full-stack billing application for a travel business that generates GST-compliant Estimates/Invoices. The user fills in minimal data (item, quantity, price, GST rate); the system auto-calculates tax, totals, and amount-in-words, then renders a print-ready document matching the company's existing Vyapar-style layout exactly.

## 2. Tech Stack
- **Frontend:** React + Tailwind CSS
- **Backend:** Java 21 + Spring Boot 3.x
- **Database:** PostgreSQL
- **Build:** Maven (backend), npm/vite (frontend)

## 3. Business Details (static/config data)
```
Business Name: Dharmdev Tourism and Travels
Address: 31 Ashadip Society, Modhera Road, Mahesana
Phone: 8469405720
Email: jayshreek762@gmail.com
GSTIN: 24EVOPK9257C1Z9
State: 24-Gujarat
```

## 4. Database Schema

### `customers`
| column | type | notes |
|---|---|---|
| id | SERIAL PK | |
| name | VARCHAR | |
| state_code | VARCHAR(2) | e.g. "24" — used for CGST/SGST vs IGST logic |
| state_name | VARCHAR | e.g. "Gujarat" |

### `items` (master catalog, optional but recommended)
| column | type | notes |
|---|---|---|
| id | SERIAL PK | |
| name | VARCHAR | e.g. "Mahesana to Ahmedabad Airport Drop" |
| hsn_sac | VARCHAR | nullable |
| default_price | NUMERIC(10,2) | |
| default_gst_rate | NUMERIC(4,2) | e.g. 18.00 |

### `estimates`
| column | type | notes |
|---|---|---|
| id | SERIAL PK | |
| estimate_no | INTEGER | auto-increment, sequential per business |
| estimate_date | DATE | |
| customer_id | FK → customers | |
| place_of_supply_state_code | VARCHAR(2) | |
| description | TEXT | nullable, e.g. "International Airport" |
| terms_and_conditions | TEXT | nullable |
| status | VARCHAR | DRAFT / FINALIZED |
| created_at | TIMESTAMP | |

### `estimate_line_items`
| column | type | notes |
|---|---|---|
| id | SERIAL PK | |
| estimate_id | FK → estimates | |
| item_name | VARCHAR | copied at time of entry (don't FK-lock to items table) |
| hsn_sac | VARCHAR | nullable |
| quantity | NUMERIC(10,2) | |
| price_per_unit | NUMERIC(10,2) | |
| gst_rate | NUMERIC(4,2) | e.g. 18.00 |

**Design rule:** Store only raw inputs (quantity, price_per_unit, gst_rate) in line items. Never store computed tax/amount columns for DRAFT estimates — recompute on every read via the calculation service. Only freeze computed values (in a separate `estimate_snapshots` table or JSON column) when status changes to FINALIZED, for audit purposes.

## 5. Calculation Algorithm (backend service — pure logic, unit-test this first)

**Per line item:**
```
taxableAmount = quantity × pricePerUnit

IF customer.stateCode == business.stateCode:
    taxType = INTRA_STATE
    cgstRate = gstRate / 2
    sgstRate = gstRate / 2
    cgstAmt = taxableAmount × cgstRate / 100
    sgstAmt = taxableAmount × sgstRate / 100
    totalTax = cgstAmt + sgstAmt
ELSE:
    taxType = INTER_STATE
    igstAmt = taxableAmount × gstRate / 100
    totalTax = igstAmt

lineAmount = taxableAmount + totalTax
```

**Aggregate across all line items:**
```
subTotal = Σ taxableAmount
totalTaxAmount = Σ totalTax
grandTotal = subTotal + totalTaxAmount
```

**Tax summary table (group by HSN/SAC + GST rate):**
Bucket line items by `(hsnSac, gstRate)`, sum taxableAmount/cgstAmt/sgstAmt/igstAmt per bucket. This produces the "HSN/SAC | Taxable Amount | CGST | SGST | Total Tax" table.

**Amount in words:**
```
grandTotalInWords = numberToWords(round(grandTotal)) + " Rupees only"
```

## 6. Backend API Endpoints
```
GET    /api/customers
POST   /api/customers
GET    /api/items
POST   /api/items
GET    /api/estimates
GET    /api/estimates/{id}          → returns full computed estimate (with tax summary)
POST   /api/estimates                → create draft
PUT    /api/estimates/{id}           → update draft (recalculates on save)
POST   /api/estimates/{id}/finalize  → freeze computed values, lock editing
GET    /api/estimates/{id}/pdf       → generate and return PDF (iText/OpenPDF)
```

## 7. Frontend Requirements — Exact Layout Match

Reproduce this structure precisely (React components + Tailwind):

**Header band**
- Centered title "Estimate"
- Left: company logo placeholder, business name (bold, large), address, phone, GSTIN, email, state — in a bordered box

**Two-column info row**
- Left: "Estimate For:" — customer name
- Right: "Estimate Details:" — No, Date, Place of Supply

**Line items table**
- Columns: # | Item Name | HSN/SAC | Quantity | Price/Unit (₹) | GST(₹) with rate shown in parentheses | Amount(₹)
- Totals row at bottom: sums Quantity, GST, Amount

**Tax Summary section (bottom-left)**
- Table: HSN/SAC | Taxable Amount | CGST (Rate% / Amt) | SGST (Rate% / Amt) | Total Tax
- TOTAL row

**Summary box (bottom-right)**
- Sub Total, Total, "Estimate Amount In Words"

**Footer band**
- Left: Description, Terms and Conditions
- Right: "For Dharmdev Tourism and Travels:" + logo + "Authorized Signatory" line

Use a clean bordered-table aesthetic matching the reference PDF (thin black borders, white background, right-aligned currency columns). This view should also be the print/PDF template.

## 8. Build Order (feature branches)
1. `feature/db-schema` — entities + repositories (Customer, Item, Estimate, EstimateLineItem)
2. `feature/gst-calculation-service` — pure calculation logic + unit tests
3. `feature/estimate-api` — REST controllers wired to the calculation service
4. `feature/frontend-estimate-form` — React form for entering line items, live totals
5. `feature/frontend-estimate-view` — the exact PDF-matching layout view
6. `feature/pdf-export` — server-side PDF generation
7. `feature/finalize-flow` — draft → finalized locking

## 9. Git Workflow
- `main` branch stays always-deployable
- One feature branch per item in Build Order above
- Merge to `main` only after that piece works end-to-end
- Commit messages describe *why*, not just *what* (e.g. "Add GST calc service with intra/inter-state branching")
