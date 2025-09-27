import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { doc, getDoc, collection, getDocs } from 'firebase/firestore';
import { db } from '@/lib/firebase';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { User, Phone, Mail, Globe, MapPin, Shield, Contact, Heart, FileText, Ban, Users } from "lucide-react";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useAuth } from '@/lib/auth';
import { useNavigate } from 'react-router-dom'; // Added import
import { Button } from "@/components/ui/button"; // Added import

const AuthorityProfile = () => {
  const { id } = useParams();
  const [tourist, setTourist] = useState<any>(null);
  const [tourists, setTourists] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate(); // Added navigate hook

  useEffect(() => {
    const fetchTourist = async () => {
      if (id) {
        const touristDoc = await getDoc(doc(db, 'tourist', id));
        if (touristDoc.exists()) {
          setTourist(touristDoc.data());
        }
        setLoading(false);
      } else {
        const touristsCollection = await getDocs(collection(db, 'tourist'));
        setTourists(touristsCollection.docs.map(doc => ({ id: doc.id, ...doc.data() })));
        setLoading(false);
      }
    };

    fetchTourist();
  }, [id]);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (id && !tourist) {
    return <div>Tourist not found</div>;
  }

  if (!id) {
    return (
      <div className="min-h-screen bg-background p-6">
        <div className="container mx-auto max-w-4xl space-y-6">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2"> {/* Added flex for alignment */}
              <CardTitle className="flex items-center">
                <User className="w-5 h-5 mr-2 text-golden" />
                Registered Tourists
              </CardTitle>
              <Button variant="outline" onClick={() => navigate('/authority')}>Back to Dashboard</Button> {/* Added Button */}
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Nationality</TableHead>
                    <TableHead>Email</TableHead>
                    <TableHead>Phone</TableHead>
                    <TableHead>Details</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {tourists.map(t => (
                    <TableRow key={t.id}>
                      <TableCell>{t.name}</TableCell>
                      <TableCell>{t.nationality}</TableCell>
                      <TableCell>{t.email}</TableCell>
                      <TableCell>{t.phone}</TableCell>
                      <TableCell>
                        <Link to={`/tourist/${t.id}`} className="text-golden underline">View</Link>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background p-6">
      <div className="container mx-auto max-w-4xl space-y-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2"> {/* Added flex for alignment */}
            <CardTitle className="flex items-center">
              <User className="w-5 h-5 mr-2 text-golden" />
              Tourist Profile
            </CardTitle>
            <Button variant="outline" onClick={() => navigate('/authority')}>Back to Dashboard</Button> {/* Added Button */}
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4 text-sm py-4">
              <p className="flex items-center"><User className="w-4 h-4 mr-2 text-muted-foreground" /><strong>Name:</strong><span className="ml-2">{tourist.name}</span></p>
              <p className="flex items-center"><User className="w-4 h-4 mr-2 text-muted-foreground" /><strong>Age:</strong><span className="ml-2">{tourist.age}</span></p>
              <p className="flex items-center"><User className="w-4 h-4 mr-2 text-muted-foreground" /><strong>Gender:</strong><span className="ml-2">{tourist.gender}</span></p>
              <p className="flex items-center"><Globe className="w-4 h-4 mr-2 text-muted-foreground" /><strong>Nationality:</strong><span className="ml-2">{tourist.nationality}</span></p>
              <p className="flex items-center"><Phone className="w-4 h-4 mr-2 text-muted-foreground" /><strong>Contact No:</strong><span className="ml-2">{tourist.phone}</span></p>
              <p className="flex items-center"><Mail className="w-4 h-4 mr-2 text-muted-foreground" /><strong>Email:</strong><span className="ml-2">{tourist.email}</span></p>
              <p className="flex items-center"><Contact className="w-4 h-4 mr-2 text-muted-foreground" /><strong>ID Number:</strong><span className="ml-2">{tourist.idNumber}</span></p>
              <p className="flex items-center"><MapPin className="w-4 h-4 mr-2 text-muted-foreground" /><strong>Visiting State:</strong><span className="ml-2">{tourist.state}</span></p>
              <div className="md:col-span-2 mt-4 pt-4 border-t">
                <h3 className="font-semibold mb-2 flex items-center text-lg">
                  <Shield className="w-5 h-5 mr-2 text-green" />
                  Emergency Contact
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4 mt-2">
                  <p className="flex items-center"><User className="w-4 h-4 mr-2 text-muted-foreground" /><strong>Name:</strong><span className="ml-2">{tourist.emergencyContactName}</span></p>
                  <p className="flex items-center"><Phone className="w-4 h-4 mr-2 text-muted-foreground" /><strong>Number:</strong><span className="ml-2">{tourist.emergencyContactNumber}</span></p>
                </div>
              </div>



              <div className="md:col-span-2 mt-4 pt-4 border-t">
                <h3 className="font-semibold mb-2 flex items-center text-lg">
                  <Heart className="w-5 h-5 mr-2 text-red-500" />
                  Medical Information
                </h3>
                <div className="space-y-4 mt-2">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4">
                    <p className="flex items-center"><Heart className="w-4 h-4 mr-2 text-muted-foreground" /><strong>Blood Group:</strong><span className="ml-2">{tourist.bloodGroup}</span></p>
                    <p className="flex items-center"><Ban className="w-4 h-4 mr-2 text-muted-foreground" /><strong>Allergies:</strong><span className="ml-2">{tourist.allergies}</span></p>
                  </div>
                  <div className="flex items-start">
                    <FileText className="w-4 h-4 mr-2 mt-1 shrink-0 text-muted-foreground" />
                    <div>
                      <strong>Previous Medical Record:</strong>
                      <p className="text-muted-foreground mt-1">{tourist.medicalRecord}</p>
                    </div>
                  </div>
                </div>
              </div>



              {tourist.photoURL && (
                <div className="md:col-span-2 mt-4 pt-4 border-t">
                  <h3 className="font-semibold mb-2 flex items-center text-lg">
                    <User className="w-5 h-5 mr-2 text-golden" />
                    Photo
                  </h3>
                  <img src={tourist.photoURL} alt={tourist.name} className="w-32 h-32 rounded-lg object-cover" />
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};





export default AuthorityProfile;