import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { AlertTriangle, MapPin, User, Phone, Mail, Clock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { collection, addDoc, updateDoc, doc, Timestamp } from 'firebase/firestore';
import { db } from '@/lib/firebase';

const TouristDistressDemo: React.FC = () => {
  const navigate = useNavigate();
  const [isCreating, setIsCreating] = useState(false);

  // Sample tourist data for demo
  const sampleTourists = [
    {
      name: 'Rajesh Kumar',
      email: 'rajesh.kumar@email.com',
      phone: '+91 98765 43210',
      location: {
        latitude: 26.1445,
        longitude: 91.7362,
        address: 'Fancy Bazar, Guwahati, Assam'
      },
      isInDistress: false,
      lastSeen: new Date(),
      emergencyType: 'Medical Emergency',
      description: 'Tourist reported feeling unwell during trek in Kamakhya Hills'
    },
    {
      name: 'Priya Singh',
      email: 'priya.singh@email.com',
      phone: '+91 87654 32109',
      location: {
        latitude: 25.5744,
        longitude: 91.8789,
        address: 'Police Bazar, Shillong, Meghalaya'
      },
      isInDistress: false,
      lastSeen: new Date(),
      emergencyType: 'Lost in Forest',
      description: 'Tourist lost contact during solo hiking in Shillong Peak area'
    },
    {
      name: 'Amit Sharma',
      email: 'amit.sharma@email.com',
      phone: '+91 76543 21098',
      location: {
        latitude: 23.8315,
        longitude: 91.2868,
        address: 'Agartala, Tripura'
      },
      isInDistress: false,
      lastSeen: new Date(),
      emergencyType: 'Vehicle Breakdown',
      description: 'Tourist stranded due to vehicle breakdown in remote area'
    }
  ];

  const [tourists, setTourists] = useState(sampleTourists);

  const createDistressedTourist = async (tourist: any) => {
    setIsCreating(true);
    try {
      // Create or update tourist in Firebase with distress status
      const touristData = {
        name: tourist.name,
        email: tourist.email,
        phone: tourist.phone,
        latitude: tourist.location.latitude,
        longitude: tourist.location.longitude,
        address: tourist.location.address,
        isInDistress: true,
        distressType: tourist.emergencyType,
        distressDescription: tourist.description,
        lastUpdatedAt: Timestamp.now(),
        createdAt: Timestamp.now()
      };

      const docRef = await addDoc(collection(db, 'tourist'), touristData);
      
      // Update local state
      const updatedTourists = tourists.map(t => 
        t.name === tourist.name ? { ...t, isInDistress: true } : t
      );
      setTourists(updatedTourists);

      alert(`Created distressed tourist: ${tourist.name}. FIR will be auto-generated. Check the E-FIR dashboard!`);
      
      // Navigate to E-FIR dashboard after a short delay
      setTimeout(() => {
        navigate('/efir');
      }, 2000);
      
    } catch (error) {
      console.error('Error creating distressed tourist:', error);
      alert('Error creating distressed tourist. Please try again.');
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <div className="p-8 bg-gradient-to-br from-red-50 to-orange-50 min-h-screen">
      <div className="container mx-auto max-w-6xl">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold bg-gradient-to-r from-red-600 to-orange-600 bg-clip-text text-transparent mb-2">
              Tourist Distress Demo
            </h1>
            <p className="text-gray-600">
              Simulate tourist distress situations to test the E-FIR auto-generation system
            </p>
          </div>
          <Button 
            onClick={() => navigate('/efir')} 
            className="bg-red-600 hover:bg-red-700"
          >
            <AlertTriangle className="w-4 h-4 mr-2" />
            View E-FIR Dashboard
          </Button>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
          {tourists.map((tourist, index) => (
            <Card key={index} className="border-orange-200 shadow-lg">
              <CardHeader className="bg-gradient-to-r from-orange-50 to-red-50">
                <CardTitle className="flex items-center justify-between">
                  <div className="flex items-center text-orange-800">
                    <User className="w-5 h-5 mr-2" />
                    {tourist.name}
                  </div>
                  {tourist.isInDistress && (
                    <Badge className="bg-red-100 text-red-800">
                      <AlertTriangle className="w-3 h-3 mr-1" />
                      IN DISTRESS
                    </Badge>
                  )}
                </CardTitle>
              </CardHeader>
              <CardContent className="p-4">
                <div className="space-y-3">
                  <div className="flex items-center text-sm text-gray-600">
                    <Mail className="w-4 h-4 mr-2" />
                    {tourist.email}
                  </div>
                  <div className="flex items-center text-sm text-gray-600">
                    <Phone className="w-4 h-4 mr-2" />
                    {tourist.phone}
                  </div>
                  <div className="flex items-center text-sm text-gray-600">
                    <MapPin className="w-4 h-4 mr-2" />
                    {tourist.location.address}
                  </div>
                  <div className="flex items-center text-sm text-gray-600">
                    <Clock className="w-4 h-4 mr-2" />
                    Last Seen: {tourist.lastSeen.toLocaleString()}
                  </div>
                  
                  <div className="mt-4 p-3 bg-red-50 rounded-lg">
                    <div className="font-medium text-red-800 mb-1">
                      Emergency Type: {tourist.emergencyType}
                    </div>
                    <div className="text-sm text-red-600">
                      {tourist.description}
                    </div>
                  </div>

                  <Button 
                    onClick={() => createDistressedTourist(tourist)}
                    disabled={tourist.isInDistress || isCreating}
                    className={`w-full mt-4 ${
                      tourist.isInDistress 
                        ? 'bg-gray-400 cursor-not-allowed' 
                        : 'bg-red-600 hover:bg-red-700'
                    }`}
                  >
                    {tourist.isInDistress ? (
                      <span>✓ Distress Created</span>
                    ) : (
                      <span>
                        <AlertTriangle className="w-4 h-4 mr-2" />
                        {isCreating ? 'Creating...' : 'Simulate Distress'}
                      </span>
                    )}
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        <Card className="border-blue-200 bg-blue-50">
          <CardHeader>
            <CardTitle className="text-blue-800">How It Works</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-2 gap-6">
              <div>
                <h3 className="font-semibold text-blue-800 mb-2">Demo Process:</h3>
                <ol className="list-decimal list-inside space-y-1 text-sm text-blue-700">
                  <li>Click "Simulate Distress" on any tourist card</li>
                  <li>Tourist data is added to Firebase with isInDistress: true</li>
                  <li>E-FIR system detects the distressed tourist</li>
                  <li>Auto-generates FIR with real tourist details</li>
                  <li>FIR appears in the E-FIR dashboard</li>
                </ol>
              </div>
              <div>
                <h3 className="font-semibold text-blue-800 mb-2">Real Features:</h3>
                <ul className="list-disc list-inside space-y-1 text-sm text-blue-700">
                  <li>Uses actual tourist data from Firebase</li>
                  <li>Real location coordinates and addresses</li>
                  <li>Automatic police station assignment</li>
                  <li>Sequential case number generation</li>
                  <li>Priority-based emergency classification</li>
                </ul>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default TouristDistressDemo;