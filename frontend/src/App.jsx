import { useState } from 'react';

const API_BASE = 'http://localhost:8080/api';

const emptyLineItem = () => ({
  itemName: '',
  hsnSac: '',
  quantity: 1,
  pricePerUnit: 0,
  gstRate: 18,
});

export default function App() {
  const [customerName, setCustomerName] = useState('');
  const [stateCode, setStateCode] = useState('24');
  const [stateName, setStateName] = useState('Gujarat');

  const [lineItems, setLineItems] = useState([emptyLineItem()]);

  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  function updateLineItem(index, field, value) {
    setLineItems(prev =>
        prev.map((item, i) => (i === index ? { ...item, [field]: value } : item))
    );
  }

  function addLineItem() {
    setLineItems(prev => [...prev, emptyLineItem()]);
  }

  function removeLineItem(index) {
    setLineItems(prev => prev.filter((_, i) => i !== index));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setResult(null);

    try {
      const customerRes = await fetch(`${API_BASE}/customers`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: customerName, stateCode, stateName }),
      });
      if (!customerRes.ok) throw new Error('Failed to create customer');
      const customer = await customerRes.json();

      const estimateRes = await fetch(`${API_BASE}/estimates`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          estimateNo: 1,
          estimateDate: new Date().toISOString().split('T')[0],
          customerId: customer.id,
          placeOfSupplyStateCode: stateCode,
          description: '',
          termsAndConditions: '',
          lineItems: lineItems.map(li => ({
            itemName: li.itemName,
            hsnSac: li.hsnSac || null,
            quantity: Number(li.quantity),
            pricePerUnit: Number(li.pricePerUnit),
            gstRate: Number(li.gstRate),
          })),
        }),
      });
      if (!estimateRes.ok) throw new Error('Failed to create estimate');
      const data = await estimateRes.json();
      setResult(data);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
      <div className="max-w-3xl mx-auto p-6">
        <h1 className="text-2xl font-bold mb-4">Dharmdev Tourism — Estimate (dev test form)</h1>

        <form onSubmit={handleSubmit} className="space-y-4 border p-4 rounded">
          <div>
            <label className="block text-sm font-medium">Customer Name</label>
            <input className="border p-2 w-full" value={customerName} onChange={e => setCustomerName(e.target.value)} required />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium">State Code</label>
              <input className="border p-2 w-full" value={stateCode} onChange={e => setStateCode(e.target.value)} required />
            </div>
            <div>
              <label className="block text-sm font-medium">State Name</label>
              <input className="border p-2 w-full" value={stateName} onChange={e => setStateName(e.target.value)} required />
            </div>
          </div>

          <hr />

          <h2 className="font-semibold">Line Items</h2>
          {lineItems.map((item, index) => (
              <div key={index} className="border p-3 rounded space-y-2 bg-gray-50">
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium">Item #{index + 1}</span>
                  {lineItems.length > 1 && (
                      <button
                          type="button"
                          onClick={() => removeLineItem(index)}
                          className="text-red-600 text-sm"
                      >
                        Remove
                      </button>
                  )}
                </div>
                <div>
                  <label className="block text-sm font-medium">Item Name</label>
                  <input
                      className="border p-2 w-full"
                      value={item.itemName}
                      onChange={e => updateLineItem(index, 'itemName', e.target.value)}
                      required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium">HSN/SAC (optional)</label>
                  <input
                      className="border p-2 w-full"
                      value={item.hsnSac}
                      onChange={e => updateLineItem(index, 'hsnSac', e.target.value)}
                  />
                </div>
                <div className="grid grid-cols-3 gap-3">
                  <div>
                    <label className="block text-sm font-medium">Quantity</label>
                    <input
                        type="number"
                        className="border p-2 w-full"
                        value={item.quantity}
                        onChange={e => updateLineItem(index, 'quantity', e.target.value)}
                        required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium">Price/Unit (₹)</label>
                    <input
                        type="number"
                        className="border p-2 w-full"
                        value={item.pricePerUnit}
                        onChange={e => updateLineItem(index, 'pricePerUnit', e.target.value)}
                        required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium">GST Rate (%)</label>
                    <input
                        type="number"
                        className="border p-2 w-full"
                        value={item.gstRate}
                        onChange={e => updateLineItem(index, 'gstRate', e.target.value)}
                        required
                    />
                  </div>
                </div>
              </div>
          ))}

          <button
              type="button"
              onClick={addLineItem}
              className="border border-blue-600 text-blue-600 px-3 py-1 rounded text-sm"
          >
            + Add Line Item
          </button>

          <div>
            <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded block">
              Create Estimate
            </button>
          </div>
        </form>

        {error && <p className="text-red-600 mt-4">Error: {error}</p>}

        {result && (
            <div className="mt-6 border p-4 rounded bg-gray-50">
              <h2 className="font-bold mb-2">Computed Result</h2>
              <table className="w-full text-sm mb-3 border-collapse">
                <thead>
                <tr className="border-b text-left">
                  <th className="py-1">Item</th>
                  <th>Qty</th>
                  <th>Price/Unit</th>
                  <th>Taxable Amt</th>
                  <th>CGST</th>
                  <th>SGST</th>
                  <th>IGST</th>
                  <th>Line Total</th>
                </tr>
                </thead>
                <tbody>
                {result.lineItems.map((li, i) => (
                    <tr key={i} className="border-b">
                      <td className="py-1">{li.lineItem.itemName}</td>
                      <td>{li.lineItem.quantity}</td>
                      <td>₹{li.lineItem.pricePerUnit}</td>
                      <td>₹{li.taxableAmount}</td>
                      <td>₹{li.cgstAmt}</td>
                      <td>₹{li.sgstAmt}</td>
                      <td>₹{li.igstAmt}</td>
                      <td>₹{li.lineAmount}</td>
                    </tr>
                ))}
                </tbody>
              </table>
              <p>Sub Total: ₹{result.subTotal}</p>
              <p>Total Tax: ₹{result.totalTax}</p>
              <p className="font-bold">Grand Total: ₹{result.grandTotal}</p>
              <p className="italic">{result.grandTotalInWords}</p>
            </div>
        )}
      </div>
  );
}