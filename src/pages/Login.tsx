import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useNavigate } from "react-router-dom";
import { UserCheck, FileText } from "lucide-react";
import { useTranslation } from "react-i18next";

const Login = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();

  return (
    <div className="min-h-screen bg-gradient-to-br from-golden-light to-green-light flex items-center justify-center p-4">
      <Card className="w-full max-w-md shadow-lg">
        <CardHeader className="space-y-1 text-center">
          <CardTitle className="text-3xl font-bold bg-gradient-to-r from-golden to-green bg-clip-text text-transparent">
            {t("raahiLogin")}
          </CardTitle>
          <p className="text-muted-foreground">{t("selectRole")}</p>
        </CardHeader>


        
        <CardContent className="space-y-6">
          <div className="grid grid-cols-2 gap-3">
            <Button type="button" variant={"outline"} onClick={() => navigate("/authority-login")} className={`h-auto p-4 flex flex-col items-center space-y-2`}>
              <UserCheck className="w-6 h-6" />
              <span className="text-sm font-medium">{t("authority")}</span>
              <span className="text-xs text-center opacity-80">{t("monitorManage")}</span>
            </Button>




            <Button type="button" variant={"outline"} onClick={() => navigate("/issuer-login")} className={`h-auto p-4 flex flex-col items-center space-y-2`}>
              <FileText className="w-6 h-6" />
              <span className="text-sm font-medium">{t("issuer")}</span>
              <span className="text-xs text-center opacity-80">{t("registerProcess")}</span>
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
export default Login;