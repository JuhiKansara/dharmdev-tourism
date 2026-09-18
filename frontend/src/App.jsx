import { useState } from 'react';

const API_BASE = 'http://localhost:8080/api';

export default function App() {
  const [customerName, setCustomerName] = useState('');
  const [stateCode, setStateCode] = useState('24');
  const [stateName, setStateName] = useState('Gujarat');

  const [itemName, setItemName] = useState('');
  const [hsnSac, setHsnSac] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [pricePerUnit, setPricePerUnit] = useState(0);
  const [gstRate, setGstRate] = useState(18);

  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setResult(null);

    try {
      // Step 1: create the customer
      const customerRes = await fetch(`${API_BASE}/customers`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: customerName, stateCode, stateName }),
      });
      if (!customerRes.ok) throw new Error('Failed to create customer');
      const customer = await customerRes.json();

      // Step 2: create the estimate using that customer's id
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
          lineItems: [
            { itemName, hsnSac: hsnSac || null, quantity: Number(quantity), pricePerUnit: Number(pricePerUnit), gstRate: Number(gstRate) },
          ],
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
      <div className="max-w-2xl mx-auto p-6">
        <h1 className="text-2xl font-bold mb-4">Dharmdev Tourism — Estimate (dev test form)</h1>

        <form onSubmit={handleSubmit} className="space-y-3 border p-4 rounded">
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

          <div>
            <label className="block text-sm font-medium">Item Name</label>
            <input className="border p-2 w-full" value={itemName} onChange={e => setItemName(e.target.value)} required />
          </div>
          <div>
            <label className="block text-sm font-medium">HSN/SAC (optional)</label>
            <input className="border p-2 w-full" value={hsnSac} onChange={e => setHsnSac(e.target.value)} />
          </div>
          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-sm font-medium">Quantity</label>
              <input type="number" className="border p-2 w-full" value={quantity} onChange={e => setQuantity(e.target.value)} required />
            </div>
            <div>
              <label className="block text-sm font-medium">Price/Unit (₹)</label>
              <input type="number" className="border p-2 w-full" value={pricePerUnit} onChange={e => setPricePerUnit(e.target.value)} required />
            </div>
            <div>
              <label className="block text-sm font-medium">GST Rate (%)</label>
              <input type="number" className="border p-2 w-full" value={gstRate} onChange={e => setGstRate(e.target.value)} required />
            </div>
          </div>

          <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded">
            Create Estimate
          </button>
        </form>

        {error && <p className="text-red-600 mt-4">Error: {error}</p>}

        {result && (
            <div className="mt-6 border p-4 rounded bg-gray-50">
              <h2 className="font-bold mb-2">Computed Result</h2>
              <p>Sub Total: ₹{result.subTotal}</p>
              <p>Total Tax: ₹{result.totalTax}</p>
              <p className="font-bold">Grand Total: ₹{result.grandTotal}</p>
              <p className="italic">{result.grandTotalInWords}</p>
            </div>
        )}
      </div>
  );
}