import { useState, useRef, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import { useToast } from "@/hooks/use-toast";
import { User, FileText, Heart, Shield, Upload } from "lucide-react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "@/components/ui/dialog";
import { auth, db, firebaseConfig } from "@/lib/firebase";
import { getAuth, createUserWithEmailAndPassword } from "firebase/auth";
import { initializeApp, getApp, deleteApp } from 'firebase/app';
import { addDoc, collection, doc, serverTimestamp, setDoc } from "firebase/firestore";
import { getStorage, ref, uploadBytes, getDownloadURL } from "firebase/storage";
import { ethers } from "ethers";
import { useAuth } from "@/lib/auth";
import { useTranslation } from "react-i18next";

const Issuer = () => {
  const { logout } = useAuth();
  const { t } = useTranslation();
  useEffect(() => {
    return () => {
      logout();
    };
  }, [logout]);

  const { toast } = useToast();
  const [open, setOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [txHash, setTxHash] = useState<string | null>(null);
  const [createdTouristId, setCreatedTouristId] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [formData, setFormData] = useState({
    name: "",
    id: "",
    age: "",
    gender: "",
    email: "",
    nationality: "",
    state: "",
    phone: "",
    emergencyContactName: "",
    emergencyContactNumber: "",
    password: "",
    passportNumber: "",
    visaNumber: "",
    visaTimeline: "",
    bloodGroup: "",
    allergies: "",
    medicalRecord: "",
    insuranceAgencyName: "",
    insuranceId: "",
  });



  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setSelectedFile(e.target.files[0]);
    }
  };
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    const requiredFields = ["name", "id", "age", "gender", "email", "nationality", "phone", "emergencyContactName", "emergencyContactNumber", "password"];
    const missing = requiredFields.filter((f) => !(formData as any)[f]);
    if (missing.length) {
      toast({ title: t("incompleteForm"), description: t("fillInFields", { fields: missing.join(", ") }), variant: "destructive" });
      return;
    }

    const secondaryAppName = 'secondary-auth-app';
    let secondaryApp;
    try {
      // 1) Create Firebase Auth account for tourist using a secondary app
      try {
        secondaryApp = initializeApp(firebaseConfig, secondaryAppName);
      } catch (error) {
        // If the app is already initialized, get the existing app
        secondaryApp = getApp(secondaryAppName);
      }
      const secondaryAuth = getAuth(secondaryApp);
      const touristCred = await createUserWithEmailAndPassword(secondaryAuth, formData.email, formData.password);
      const touristUid = touristCred.user.uid;

      // 2) Write 'users/{uid}' with role=tourist
      await setDoc(doc(db, "users", touristUid), {
        role: "tourist",
        email: formData.email,
        createdAt: serverTimestamp(),
      });

      // 3) Ask MetaMask for Polygon Amoy transaction to get a hash (0-value tx is fine)
      let hash: string | null = null;
      // @ts-ignore
      if (window.ethereum) {
        // @ts-ignore
        const provider = new ethers.BrowserProvider(window.ethereum);
        const signer = await provider.getSigner();
        const network = await provider.getNetwork();
        // Polygon Amoy chainId is 0x13882 (80002)
        const amoyChainId = 80002n;
        if (network.chainId !== amoyChainId) {
          // Try to switch network
          await (window as any).ethereum.request({
            method: "wallet_switchEthereumChain",
            params: [{ chainId: "0x13882" }],
          }).catch(async () => {
            // Try to add the network if not present
            await (window as any).ethereum.request({
              method: "wallet_addEthereumChain",
              params: [{
                chainId: "0x13882",
                chainName: "Polygon Amoy",
                nativeCurrency: { name: "MATIC", symbol: "MATIC", decimals: 18 },
                rpcUrls: ["https://rpc-amoy.polygon.technology"],
                blockExplorerUrls: ["https://www.oklink.com/amoy"],
              }],
            });
          });
        }
        const tx = await signer.sendTransaction({ to: await signer.getAddress(), value: 0n, gasLimit: 30000 });
        const receipt = await tx.wait();
        hash = receipt?.hash ?? tx.hash;
      }




      let photoURL = "";
      if (selectedFile) {
        const storage = getStorage();
        const storageRef = ref(storage, `tourist-photos/${touristUid}`);
        await uploadBytes(storageRef, selectedFile);
        photoURL = await getDownloadURL(storageRef);
      }
      const touristDocRef = doc(db, "tourist", touristUid);
      await setDoc(touristDocRef, {
        uid: touristUid,
        role: "tourist",
        name: formData.name,
        idNumber: formData.id,
        age: formData.age,
        gender: formData.gender,
        email: formData.email,
        nationality: formData.nationality,
        state: formData.state,
        phone: formData.phone,
        emergencyContactName: formData.emergencyContactName,
        emergencyContactNumber: formData.emergencyContactNumber,
        passportNumber: formData.passportNumber,
        visaNumber: formData.visaNumber,
        visaTimeline: formData.visaTimeline,
        bloodGroup: formData.bloodGroup,
        allergies: formData.allergies,
        medicalRecord: formData.medicalRecord,
        insuranceAgencyName: formData.insuranceAgencyName,
        insuranceId: formData.insuranceId,
        blockchainTxHash: hash,
        photoURL: photoURL,
        createdAt: serverTimestamp(),


      });

      setTxHash(hash);
      setCreatedTouristId(touristUid);
      setOpen(true);
      setFormData({
        name: "",
        id: "",
        age: "",
        gender: "",
        email: "",
        nationality: "",
        state: "",
        phone: "",
        emergencyContactName: "",
        emergencyContactNumber: "",
        password: "",
        passportNumber: "",
        visaNumber: "",
        visaTimeline: "",
        bloodGroup: "",
        allergies: "",
        medicalRecord: "",
        insuranceAgencyName: "",
        insuranceId: "",
      });
      setSelectedFile(null);
    } catch (err: any) {
      if (err.code === 'auth/email-already-in-use') {
        toast({ title: t("error"), description: t("emailAlreadyRegistered"), variant: "destructive" });
      } else {
        toast({ title: t("error"), description: err.message ?? t("failedToRegister"), variant: "destructive" });
      }
    } finally {
      if (secondaryApp) {
        deleteApp(secondaryApp);
      }
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-background p-6">
      <div className="container mx-auto max-w-4xl">
        <div className="mb-8 text-center">
          <h1 className="text-3xl md:text-4xl font-bold text-foreground mb-2 bg-gradient-to-r from-golden to-green bg-clip-text text-transparent">{t("travelerRegistration")}</h1>
          <p className="text-muted-foreground">{t("fillAllFields")}</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-8">
          <Card className="border-golden/30">
            <CardHeader className="bg-gradient-to-r from-golden/10 to-green/10 rounded-t-lg">
              <CardTitle className="flex items-center text-foreground">
                <User className="w-5 h-5 mr-2 text-golden" />
                {t("personalInformation")}
              </CardTitle>
            </CardHeader>
            <CardContent className="grid md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="name">{t("fullName")}</Label>
                <Input id="name" value={formData.name} onChange={(e) => handleInputChange("name", e.target.value)} placeholder={t("enterFullName")} required />
              </div>
              <div className="space-y-2">
                <Label htmlFor="id">{t("idNumber")}</Label>
                <Input id="id" value={formData.id} onChange={(e) => handleInputChange("id", e.target.value)} placeholder={t("idPlaceholder")} required />
              </div>
              <div className="space-y-2">
                <Label htmlFor="password">{t("passwordLabel")}</Label>
                <Input id="password" type="password" value={formData.password} onChange={(e) => handleInputChange("password", e.target.value)} placeholder={t("passwordPlaceholder")} required />
              </div>
              <div className="space-y-2">
                <Label htmlFor="age">{t("ageLabel")}</Label>
                <Input id="age" type="number" value={formData.age} onChange={(e) => handleInputChange("age", e.target.value)} placeholder={t("agePlaceholder")} required />
              </div>
              <div className="space-y-2">
                <Label htmlFor="gender">{t("genderLabel")}</Label>
                <Select onValueChange={(value) => handleInputChange("gender", value)}>
                  <SelectTrigger>
                    <SelectValue placeholder={t("genderPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="male">{t("male")}</SelectItem>
                    <SelectItem value="female">{t("female")}</SelectItem>
                    <SelectItem value="other">{t("other")}</SelectItem>
                    <SelectItem value="prefer-not-to-say">{t("preferNotToSay")}</SelectItem>
                  </SelectContent>
                </Select>
              </div>



              <div className="space-y-2">
                <Label htmlFor="email">{t("emailLabel")}</Label>
                <Input id="email" type="email" value={formData.email} onChange={(e) => handleInputChange("email", e.target.value)} placeholder={t("emailPlaceholder")} required />
              </div>
              <div className="space-y-2">
                <Label htmlFor="nationality">{t("nationalityLabel")}</Label>
                <Input id="nationality" value={formData.nationality} onChange={(e) => handleInputChange("nationality", e.target.value)} placeholder={t("nationalityPlaceholder")} required />
              </div>
              <div className="space-y-2">
                <Label htmlFor="state">{t("stateLabel")}</Label>
                <Input id="state" value={formData.state} onChange={(e) => handleInputChange("state", e.target.value)} placeholder={t("statePlaceholder")} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="phone">{t("phoneLabel")}</Label>
                <Input id="phone" type="tel" value={formData.phone} onChange={(e) => handleInputChange("phone", e.target.value)} placeholder={t("phonePlaceholder")} required />
              </div>
            </CardContent>
          </Card>





          <Card className="border-green/30">
            <CardHeader className="bg-gradient-to-r from-green/10 to-golden/10 rounded-t-lg">
              <CardTitle className="flex items-center text-foreground">
                <Heart className="w-5 h-5 mr-2 text-green" />
                {t("emergencyContact")}
              </CardTitle>
            </CardHeader>
            <CardContent className="grid md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="emergencyContactName">{t("emergencyContactName")}</Label>
                <Input id="emergencyContactName" value={formData.emergencyContactName} onChange={(e) => handleInputChange("emergencyContactName", e.target.value)} placeholder={t("emergencyContactNamePlaceholder")} required />
              </div>
              <div className="space-y-2">
                <Label htmlFor="emergencyContactNumber">{t("emergencyContactNumber")}</Label>
                <Input id="emergencyContactNumber" type="tel" value={formData.emergencyContactNumber} onChange={(e) => handleInputChange("emergencyContactNumber", e.target.value)} placeholder={t("phonePlaceholder")} required />
              </div>
            </CardContent>
          </Card>





          <Card className="border-golden/30">
            <CardHeader className="bg-gradient-to-r from-golden/10 to-green/10 rounded-t-lg">
              <CardTitle className="flex items-center text-foreground">
                <FileText className="w-5 h-5 mr-2 text-golden" />
                {t("travelDocuments")}
              </CardTitle>
            </CardHeader>
            <CardContent className="grid md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="passportNumber">{t("passportNumber")}</Label>
                <Input id="passportNumber" value={formData.passportNumber} onChange={(e) => handleInputChange("passportNumber", e.target.value)} placeholder={t("passportNumberPlaceholder")} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="visaNumber">{t("visaNumber")}</Label>
                <Input id="visaNumber" value={formData.visaNumber} onChange={(e) => handleInputChange("visaNumber", e.target.value)} placeholder={t("visaNumberPlaceholder")} />
              </div>
              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="visaTimeline">{t("visaTimeline")}</Label>
                <Input id="visaTimeline" value={formData.visaTimeline} onChange={(e) => handleInputChange("visaTimeline", e.target.value)} placeholder={t("visaTimelinePlaceholder")} />
              </div>
            </CardContent>
          </Card>







          <Card className="border-green/30">
            <CardHeader className="bg-gradient-to-r from-green/10 to-golden/10 rounded-t-lg">
              <CardTitle className="flex items-center text-foreground">
                <Heart className="w-5 h-5 mr-2 text-green" />
                {t("medicalInformation")}
              </CardTitle>
            </CardHeader>
            <CardContent className="grid md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="bloodGroup">{t("bloodGroup")}</Label>
                <Select onValueChange={(value) => handleInputChange("bloodGroup", value)}>
                  <SelectTrigger>
                    <SelectValue placeholder={t("bloodGroupPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="A+">A+</SelectItem>
                    <SelectItem value="A-">A-</SelectItem>
                    <SelectItem value="B+">B+</SelectItem>
                    <SelectItem value="B-">B-</SelectItem>
                    <SelectItem value="AB+">AB+</SelectItem>
                    <SelectItem value="AB-">AB-</SelectItem>
                    <SelectItem value="O+">O+</SelectItem>
                    <SelectItem value="O-">O-</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="allergies">{t("allergies")}</Label>
                <Input id="allergies" value={formData.allergies} onChange={(e) => handleInputChange("allergies", e.target.value)} placeholder={t("allergiesPlaceholder")} />
              </div>
              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="medicalRecord">{t("previousMedicalRecord")}</Label>
                <Textarea id="medicalRecord" value={formData.medicalRecord} onChange={(e) => handleInputChange("medicalRecord", e.target.value)} placeholder={t("previousMedicalRecordPlaceholder")} rows={3} />
              </div>
            </CardContent>
          </Card>

          <Card className="border-green/30">
            <CardHeader className="bg-gradient-to-r from-green/10 to-golden/10 rounded-t-lg">
              <CardTitle className="flex items-center text-foreground">
                <Shield className="w-5 h-5 mr-2 text-green" />
                {t("insuranceInformation")}
              </CardTitle>
            </CardHeader>
            <CardContent className="grid md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="insuranceAgencyName">{t("insuranceAgencyName")}</Label>
                <Input id="insuranceAgencyName" value={formData.insuranceAgencyName} onChange={(e) => handleInputChange("insuranceAgencyName", e.target.value)} placeholder={t("insuranceAgencyNamePlaceholder")} />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="insuranceId">{t("insuranceId")}</Label>
                <Input id="insuranceId" value={formData.insuranceId} onChange={(e) => handleInputChange("insuranceId", e.target.value)} placeholder={t("insuranceIdPlaceholder")} />
              </div>
            </CardContent>
          </Card>






          <Card className="border-golden/30">
            <CardHeader className="bg-gradient-to-r from-golden/10 to-green/10 rounded-t-lg">
              <CardTitle className="flex items-center text-foreground">
                <Upload className="w-5 h-5 mr-2 text-golden" />
                {t("photoUpload")}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="border-2 border-dashed border-golden/40 rounded-lg p-8 text-center bg-gradient-to-br from-golden/5 to-green/5">
                <Upload className="w-12 h-12 mx-auto text-muted-foreground mb-4" />
                <p className="text-muted-foreground mb-2">{selectedFile ? selectedFile.name : t("photoUploadPlaceholder")}</p>
                <Input type="file" ref={fileInputRef} onChange={handleFileChange} className="hidden" accept="image/png, image/jpeg, image/gif" />
                <Button type="button" variant="outline" size="sm" className="border-golden text-foreground" onClick={() => fileInputRef.current?.click()}>
                  {t("chooseFile")}
                </Button>
                <p className="text-xs text-muted-foreground mt-2">
                  {t("supportedFormats")}
                </p>
              </div>
            </CardContent>
          </Card>

          <Separator />




          <div className="flex justify-center">
            <Button 
              type="submit" 
              size="lg"
              className="bg-gradient-to-r from-golden to-green hover:from-golden-dark hover:to-green-dark text-white px-12"
              disabled={isSubmitting}
            >
              {isSubmitting ? t("submitting") : t("submitRegistration")}
            </Button>
          </div>
        </form>





        <Dialog open={open} onOpenChange={setOpen}>
          <DialogContent className="sm:max-w-md border-golden/40 bg-gradient-to-br from-golden/5 to-green/5">
            <DialogHeader>
              <DialogTitle className="text-foreground">{t("submittedSuccessfully")}</DialogTitle>
              <DialogDescription className="text-muted-foreground">
                {t("travelerRegistered")}
              </DialogDescription>
            </DialogHeader>
            <div className="py-2 text-sm text-foreground">
              <p>{t("touristProfileLink")}</p>
              <div className="flex items-center space-x-2 mt-2">
                {createdTouristId && <Input value={`${window.location.origin}/tourist/${createdTouristId}`} readOnly />}
                {createdTouristId && <Button onClick={() => navigator.clipboard.writeText(`${window.location.origin}/tourist/${createdTouristId}`)}>{t("copy")}</Button>}
              </div>
              {txHash && (
                <div className="mt-4">
                  <p>{t("blockchainTransactionHash")}</p>
                  <a href={`https://www.oklink.com/amoy/tx/${txHash}`} target="_blank" rel="noopener noreferrer" className="text-blue-500 hover:underline">{txHash}</a>
                </div>
              )}
            </div>
            <DialogFooter>
              <Button onClick={() => setOpen(false)} className="bg-gradient-to-r from-golden to-green hover:from-golden-dark hover:to-green-dark text-white">{t("close")}</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    </div>
  );
};
export default Issuer;