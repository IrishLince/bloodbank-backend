// MongoDB script to update existing hospital_requests documents
// Adds sample blood_items array to existing requests that don't have it

// Connect to the database
use('redsource');

// Update hospital_requests collection to add blood_items field where it's missing
db.hospital_requests.updateMany(
    { 
        $or: [
            { blood_items: { $exists: false } },
            { blood_items: null },
            { blood_items: [] }
        ]
    },
    { 
        $set: { 
            blood_items: [
                {
                    blood_type: "A+",
                    quantity: 2,
                    urgency: "High"
                },
                {
                    blood_type: "O-",
                    quantity: 1,
                    urgency: "Critical"
                }
            ]
        }
    }
);

print("Updated hospital_requests documents with sample blood_items data");

// Show count of updated documents
var count = db.hospital_requests.countDocuments({ blood_items: { $exists: true, $ne: [] } });
print("Total documents with blood_items: " + count);
