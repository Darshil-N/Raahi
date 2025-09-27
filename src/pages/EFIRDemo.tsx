import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { AlertTriangle, MapPin, User } from 'lucide-react';
import { collection, doc, setDoc, Timestamp } from 'firebase/firestore';
import { db } from '@/lib/firebase';
import { toast } from 'sonner';

// Demo component for testing E-FIR auto-generation
const EFIRDemo: React.FC = () => {
  const [isSimulating, setIsSimulating] = useState(false);
  const [touristData, setTouristData] = useState({
    name: 'Test Tourist',
    email: 'test.tourist@example.com',
    phone: '+91 9876543210',
    latitude: 26.1445,
    longitude: 91.7362
  });

  const simulateDistressTourist = async () => {
    setIsSimulating(true);
    try {
      // Create a test tourist UID
      const testTouristUid = `test-tourist-${Date.now()}`;
      
      // Create tourist document in distress
      const touristDocRef = doc(db, 'tourist', testTouristUid);
      await setDoc(touristDocRef, {
        uid: testTouristUid,
        name: touristData.name,
        email: touristData.email,
        phone: touristData.phone,
        isInDistress: true, // This will trigger auto-FIR generation
        location: {
          latitude: touristData.latitude,
          longitude: touristData.longitude
        },
        latitude: touristData.latitude, // For backward compatibility
        longitude: touristData.longitude, // For backward compatibility
        lastUpdatedAt: Timestamp.now(),
        createdAt: Timestamp.now()
      });

      toast.success('Tourist in distress simulated! Check the E-FIR dashboard for auto-generated FIR.');
    } catch (error) {
      console.error('Error simulating distress:', error);
      toast.error('Failed to simulate tourist distress');
    } finally {
      setIsSimulating(false);
    }
  };

  return (
    <div className="p-8 bg-gradient-to-br from-blue-50 to-indigo-50 min-h-screen">
      <div className="container mx-auto max-w-2xl">
        <Card className="border-red-200 shadow-lg">
          <CardHeader className="bg-gradient-to-r from-red-50 to-pink-50">
            <CardTitle className="flex items-center text-red-800">
              <AlertTriangle className="w-6 h-6 mr-3" />
              E-FIR Demo - Simulate Tourist Distress
            </CardTitle>
          </CardHeader>
          <CardContent className="p-6">
            <div className="space-y-4">
              <div>
                <Label htmlFor="name">Tourist Name</Label>
                <Input
                  id="name"
                  value={touristData.name}
                  onChange={(e) => setTouristData({ ...touristData, name: e.target.value })}
                  className="mt-1"
                />
              </div>
              
              <div>
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  value={touristData.email}
                  onChange={(e) => setTouristData({ ...touristData, email: e.target.value })}
                  className="mt-1"
                />
              </div>
              
              <div>
                <Label htmlFor="phone">Phone</Label>
                <Input
                  id="phone"
                  value={touristData.phone}
                  onChange={(e) => setTouristData({ ...touristData, phone: e.target.value })}
                  className="mt-1"
                />
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="latitude">Latitude</Label>
                  <Input
                    id="latitude"
                    type="number"
                    step="0.000001"
                    value={touristData.latitude}
                    onChange={(e) => setTouristData({ ...touristData, latitude: parseFloat(e.target.value) })}
                    className="mt-1"
                  />
                </div>
                <div>
                  <Label htmlFor="longitude">Longitude</Label>
                  <Input
                    id="longitude"
                    type="number"
                    step="0.000001"
                    value={touristData.longitude}
                    onChange={(e) => setTouristData({ ...touristData, longitude: parseFloat(e.target.value) })}
                    className="mt-1"
                  />
                </div>
              </div>
              
              <div className="pt-4">
                <Button
                  onClick={simulateDistressTourist}
                  disabled={isSimulating}
                  className="w-full bg-red-600 hover:bg-red-700"
                >
                  <AlertTriangle className="w-4 h-4 mr-2" />
                  {isSimulating ? 'Simulating Distress...' : 'Simulate Tourist in Distress (Auto-Generate FIR)'}
                </Button>
              </div>
              
              <div className="text-sm text-gray-600 bg-gray-50 p-4 rounded-lg">
                <p className="font-medium mb-2">What happens when you click simulate:</p>
                <ul className="list-disc pl-5 space-y-1">
                  <li>Creates a tourist document with <code>isInDistress: true</code></li>
                  <li>The distress monitoring service detects the change</li>
                  <li>An E-FIR is automatically generated with high priority</li>
                  <li>The FIR appears in the Authority dashboard E-FIR section</li>
                  <li>Includes location, tourist details, and emergency response protocol</li>
                </ul>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default EFIRDemo;