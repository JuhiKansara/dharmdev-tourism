import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

const API_BASE = 'http://localhost:8080/api';

const BUSINESS = {
    name: 'Dharmdev Tourism and Travels',
    address: '31 Ashadip Society, Modhera Road, Mahesana',
    phone: '8469405720',
    email: 'jayshreek762@gmail.com',
    gstin: '24EVOPK9257C1Z9',
    state: '24-Gujarat',
};

export default function EstimatePreviewPage() {
    const { id } = useParams();
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetch(`${API_BASE}/estimates/${id}`)
            .then(res => {
                if (!res.ok) throw new Error('Estimate not found');
                return res.json();
            })
            .then(setData)
            .catch(err => setError(err.message));
    }, [id]);

    if (error) return <p className="text-red-600 p-6">{error}</p>;
    if (!data) return <p className="p-6">Loading...</p>;

    return (
        <div className="max-w-4xl mx-auto p-6 bg-white text-black text-sm">
            <h1 className="text-center text-2xl font-bold mb-4">Estimate</h1>

            {/* Header band */}
            <div className="border border-black flex justify-between p-3 mb-0">
                <div>
                    <div className="font-bold text-lg">{BUSINESS.name}</div>
                    <div>{BUSINESS.address}</div>
                    <div>Phone: <span className="font-semibold">{BUSINESS.phone}</span></div>
                    <div>GSTIN: <span className="font-semibold">{BUSINESS.gstin}</span></div>
                </div>
                <div className="text-right">
                    <div>Email: <span className="font-semibold">{BUSINESS.email}</span></div>
                    <div>State: <span className="font-semibold">{BUSINESS.state}</span></div>
                </div>
            </div>

            {/* Estimate For / Details row */}
            <div className="border border-t-0 border-black grid grid-cols-2">
                <div className="p-3 border-r border-black">
                    <div className="font-semibold">Estimate For:</div>
                    <div>{data.customerName}</div>
                </div>
                <div className="p-3">
                    <div className="font-semibold">Estimate Details:</div>
                    <div>No: {data.estimateNo}</div>
                    <div>Date: {data.estimateDate}</div>
                    <div>Place of Supply: {data.placeOfSupplyStateCode}-Gujarat</div>
                </div>
            </div>

            {/* Line items table */}
            <table className="w-full border-collapse border border-t-0 border-black">
                <thead>
                <tr className="border-b border-black text-left">
                    <th className="p-2 border-r border-black">#</th>
                    <th className="p-2 border-r border-black">Item Name</th>
                    <th className="p-2 border-r border-black">HSN/SAC</th>
                    <th className="p-2 border-r border-black text-right">Quantity</th>
                    <th className="p-2 border-r border-black text-right">Price/Unit (₹)</th>
                    <th className="p-2 border-r border-black text-right">GST(₹)</th>
                    <th className="p-2 text-right">Amount(₹)</th>
                </tr>
                </thead>
                <tbody>
                {data.lineItems.map((li, i) => (
                    <tr key={i} className="border-b border-black">
                        <td className="p-2 border-r border-black">{i + 1}</td>
                        <td className="p-2 border-r border-black">{li.lineItem.itemName}</td>
                        <td className="p-2 border-r border-black">{li.lineItem.hsnSac || ''}</td>
                        <td className="p-2 border-r border-black text-right">{li.lineItem.quantity}</td>
                        <td className="p-2 border-r border-black text-right">₹{li.lineItem.pricePerUnit}</td>
                        <td className="p-2 border-r border-black text-right">₹{li.totalTax} ({li.lineItem.gstRate}%)</td>
                        <td className="p-2 text-right">₹{li.lineAmount}</td>
                    </tr>
                ))}
                <tr className="font-bold">
                    <td className="p-2 border-r border-black" colSpan={3}>Total</td>
                    <td className="p-2 border-r border-black text-right">
                        {data.lineItems.reduce((s, li) => s + Number(li.lineItem.quantity), 0)}
                    </td>
                    <td className="p-2 border-r border-black"></td>
                    <td className="p-2 border-r border-black text-right">₹{data.totalTax}</td>
                    <td className="p-2 text-right">₹{data.grandTotal}</td>
                </tr>
                </tbody>
            </table>

            {/* Tax summary + amount box */}
            <div className="border border-t-0 border-black grid grid-cols-2">
                <div className="border-r border-black">
                    <div className="font-semibold p-2 border-b border-black">Tax Summary:</div>
                    <table className="w-full border-collapse text-xs">
                        <thead>
                        <tr className="border-b border-black">
                            <th className="p-1 border-r border-black">HSN/SAC</th>
                            <th className="p-1 border-r border-black">Taxable Amt</th>
                            <th className="p-1 border-r border-black">CGST</th>
                            <th className="p-1 border-r border-black">SGST</th>
                            <th className="p-1">Total Tax</th>
                        </tr>
                        </thead>
                        <tbody>
                        {data.taxSummary.map((row, i) => (
                            <tr key={i} className="border-b border-black">
                                <td className="p-1 border-r border-black">{row.hsnSac || ''}</td>
                                <td className="p-1 border-r border-black text-right">₹{row.taxableAmount}</td>
                                <td className="p-1 border-r border-black text-right">₹{row.cgstAmt}</td>
                                <td className="p-1 border-r border-black text-right">₹{row.sgstAmt}</td>
                                <td className="p-1 text-right">₹{row.totalTax}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
                <div className="p-3">
                    <div className="flex justify-between"><span>Sub Total</span><span>₹{data.subTotal}</span></div>
                    <div className="flex justify-between font-bold"><span>Total</span><span>₹{data.grandTotal}</span></div>
                    <div className="mt-2">
                        <div className="font-semibold">Estimate Amount In Words:</div>
                        <div>{data.grandTotalInWords}</div>
                    </div>
                </div>
            </div>

            {/* Footer */}
            <div className="border border-t-0 border-black grid grid-cols-2">
                <div className="p-3 border-r border-black">
                    <div className="font-semibold">Description:</div>
                    <div>{data.description}</div>
                </div>
                <div className="p-3">
                    <div className="font-semibold">Terms And Conditions:</div>
                    <div>{data.termsAndConditions}</div>
                    <div className="mt-4 font-semibold">For Dharmdev Tourism and Travels:</div>
                    <div className="mt-8 text-right">Authorized Signatory</div>
                </div>
            </div>
        </div>
    );
}