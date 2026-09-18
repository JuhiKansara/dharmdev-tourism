import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const API_BASE = 'http://localhost:8080/api';

const emptyLineItem = () => ({
    itemName: '',
    hsnSac: '',
    quantity: 1,
    pricePerUnit: 0,
    gstRate: 18,
});

export default function CreateEstimatePage() {
    const navigate = useNavigate();

    const [customerName, setCustomerName] = useState('');
    const [stateCode, setStateCode] = useState('24');
    const [stateName, setStateName] = useState('Gujarat');
    const [estimateNo, setEstimateNo] = useState(1);
    const [description, setDescription] = useState('');
    const [termsAndConditions, setTermsAndConditions] = useState('');
    const [lineItems, setLineItems] = useState([emptyLineItem()]);
    const [error, setError] = useState(null);

    function updateLineItem(index, field, value) {
        setLineItems(prev => prev.map((item, i) => (i === index ? { ...item, [field]: value } : item)));
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
                    estimateNo: Number(estimateNo),
                    estimateDate: new Date().toISOString().split('T')[0],
                    customerId: customer.id,
                    placeOfSupplyStateCode: stateCode,
                    description,
                    termsAndConditions,
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

            // Navigate to the PDF-style preview, fetched fresh by ID
            navigate(`/estimates/${data.estimateId}`);
        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <div className="max-w-3xl mx-auto p-6">
            <h1 className="text-2xl font-bold mb-4">Dharmdev Tourism — New Estimate</h1>

            <form onSubmit={handleSubmit} className="space-y-4 border p-4 rounded">
                <div className="grid grid-cols-2 gap-3">
                    <div>
                        <label className="block text-sm font-medium">Estimate No</label>
                        <input type="number" className="border p-2 w-full" value={estimateNo} onChange={e => setEstimateNo(e.target.value)} required />
                    </div>
                </div>

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

                <div>
                    <label className="block text-sm font-medium">Description (optional)</label>
                    <input className="border p-2 w-full" value={description} onChange={e => setDescription(e.target.value)} />
                </div>
                <div>
                    <label className="block text-sm font-medium">Terms and Conditions (optional)</label>
                    <input className="border p-2 w-full" value={termsAndConditions} onChange={e => setTermsAndConditions(e.target.value)} />
                </div>

                <hr />
                <h2 className="font-semibold">Line Items</h2>
                {lineItems.map((item, index) => (
                    <div key={index} className="border p-3 rounded space-y-2 bg-gray-50">
                        <div className="flex justify-between items-center">
                            <span className="text-sm font-medium">Item #{index + 1}</span>
                            {lineItems.length > 1 && (
                                <button type="button" onClick={() => removeLineItem(index)} className="text-red-600 text-sm">Remove</button>
                            )}
                        </div>
                        <div>
                            <label className="block text-sm font-medium">Item Name</label>
                            <input className="border p-2 w-full" value={item.itemName} onChange={e => updateLineItem(index, 'itemName', e.target.value)} required />
                        </div>
                        <div>
                            <label className="block text-sm font-medium">HSN/SAC (optional)</label>
                            <input className="border p-2 w-full" value={item.hsnSac} onChange={e => updateLineItem(index, 'hsnSac', e.target.value)} />
                        </div>
                        <div className="grid grid-cols-3 gap-3">
                            <div>
                                <label className="block text-sm font-medium">Quantity</label>
                                <input type="number" className="border p-2 w-full" value={item.quantity} onChange={e => updateLineItem(index, 'quantity', e.target.value)} required />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Price/Unit (₹)</label>
                                <input type="number" className="border p-2 w-full" value={item.pricePerUnit} onChange={e => updateLineItem(index, 'pricePerUnit', e.target.value)} required />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">GST Rate (%)</label>
                                <input type="number" className="border p-2 w-full" value={item.gstRate} onChange={e => updateLineItem(index, 'gstRate', e.target.value)} required />
                            </div>
                        </div>
                    </div>
                ))}
                <button type="button" onClick={addLineItem} className="border border-blue-600 text-blue-600 px-3 py-1 rounded text-sm">
                    + Add Line Item
                </button>

                <div>
                    <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded block">Create Estimate</button>
                </div>
            </form>

            {error && <p className="text-red-600 mt-4">Error: {error}</p>}
        </div>
    );
}