import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useNavigate } from "react-router-dom";
import { User, Lock } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { signInWithEmailAndPassword } from "firebase/auth";
import { auth, db } from "@/lib/firebase";
import { doc, getDoc } from "firebase/firestore";
import { useTranslation } from "react-i18next";

const IssuerLogin = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();
  const { toast } = useToast();
  const { t } = useTranslation();
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      toast({ title: t("error"), description: t("enterEmailPassword"), variant: "destructive" });
      return;
    }
    try {
      const cred = await signInWithEmailAndPassword(auth, email, password);
      const userDoc = await getDoc(doc(db, "users", cred.user.uid));
      const role = userDoc.exists() ? (userDoc.data() as any).role : null;
      if (role !== "issuer") {
        await auth.signOut();
        toast({ title: t("accessDenied"), description: t("notIssuer"), variant: "destructive" });
        return;
      }
      toast({ title: t("loginSuccessful"), description: t("welcomeIssuer") });
      navigate(`/issuer`);
    } catch (err: any) {
      toast({ title: t("loginFailed"), description: err.message ?? t("invalidCredentials"), variant: "destructive" });
    }
  };



  return (
    <div className="min-h-screen bg-gradient-to-br from-golden-light to-green-light flex items-center justify-center p-4">
      <Card className="w-full max-w-md shadow-lg">
        <CardHeader className="space-y-1 text-center">
          <CardTitle className="text-3xl font-bold bg-gradient-to-r from-golden to-green bg-clip-text text-transparent">
            {t("issuerLogin")}
          </CardTitle>
          <p className="text-muted-foreground">{t("accessYourAccount")}</p>
        </CardHeader>
        <CardContent className="space-y-6">
          <form onSubmit={handleLogin} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">{t("email")}</Label>
              <div className="relative">
                <User className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input id="email" type="email" placeholder={t("enterYourEmail")} value={email} onChange={(e) => setEmail(e.target.value)} className="pl-10" required />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">{t("password")}</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input id="password" type="password" placeholder={t("enterYourPassword")} value={password} onChange={(e) => setPassword(e.target.value)} className="pl-10" required />
              </div>
            </div>
            <Button type="submit" className="w-full bg-gradient-to-r from-golden to-green hover:from-golden-dark hover:to-green-dark text-white" size="lg">
              {t("login")}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};
export default IssuerLogin;
