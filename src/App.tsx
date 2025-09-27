import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate, useLocation } from "react-router-dom";
import Header from "@/components/Header";
import Homepage from "./pages/Homepage";
import Login from "./pages/Login";
import Authority from "./pages/Authority";
import Issuer from "./pages/Issuer";
import NotFound from "./pages/NotFound";
import AuthorityProfile from "./pages/authority-profile";
import { AuthProvider, useAuth } from "./lib/auth";
import AuthorityLogin from "./pages/AuthorityLogin";
import IssuerLogin from "./pages/IssuerLogin";
import TouristDetails from "./pages/TouristDetails";
import TicketBlockage from "./pages/TicketBlockage";
import EFIR from "./pages/EFIR";
import FIRDetails from "./pages/FIRDetails";
import EFIRDemo from "./pages/EFIRDemo";
import TouristDistressDemo from "./pages/TouristDistressDemo";

const queryClient = new QueryClient();

const RequireRole = ({ roles, children }: { roles: ("issuer" | "authority")[]; children: JSX.Element }) => {
  const { user, role: currentRole, loading } = useAuth();
  const location = useLocation();

  if (loading) return <div>Loading...</div>;
  if (!user) {
    sessionStorage.setItem("redirectUrl", location.pathname);
    return <Navigate to={`/login`} replace />;
  }
  if (!currentRole || !roles.includes(currentRole as any)) return <Navigate to={`/`} replace />;

  return children;
};

const App = () => (

  <QueryClientProvider client={queryClient}>

    <TooltipProvider>

      <Toaster />


      <Sonner />

      <AuthProvider>
        <BrowserRouter>
          <Header />
          <Routes>
            <Route path="/" element={<Homepage />} />
            <Route path="/login" element={<Login />} />
            <Route path="/authority-login" element={<AuthorityLogin />} />
            <Route path="/issuer-login" element={<IssuerLogin />} />
            <Route
              path="/authority"
              element={
                <RequireRole roles={["authority"]}>
                  <Authority />
                </RequireRole>
              }
            />
            <Route
              path="/issuer"
              element={
                <RequireRole roles={["issuer"]}>
                  <Issuer />
                </RequireRole>
              }
            />


            <Route
              path="/tourist/:id"
              element={
                <RequireRole roles={["authority", "issuer"]}>
                  <TouristDetails />
                </RequireRole>
              }
            />




            <Route
              path="/authority-profile"
              element={
                <RequireRole roles={["authority"]}>
                  <AuthorityProfile />
                </RequireRole>
              }
            />
            

            <Route
              path="/ticket-blockage"
              element={
                <RequireRole roles={["authority"]}>
                  <TicketBlockage />
                </RequireRole>
              }
            />

            <Route
              path="/e-fir"
              element={
                <RequireRole roles={["authority"]}>
                  <EFIR />
                </RequireRole>
              }
            />

            <Route path="/fir-details/:id" element={<FIRDetails />} />
            
            {/* Demo route for testing E-FIR system */}
            <Route
              path="/efir-demo"
              element={
                <RequireRole roles={["authority"]}>
                  <EFIRDemo />
                </RequireRole>
              }
            />

            {/* Tourist distress simulation demo */}
            <Route
              path="/tourist-distress-demo"
              element={
                <RequireRole roles={["authority"]}>
                  <TouristDistressDemo />
                </RequireRole>
              }
            />

            <Route path="*" element={<NotFound />} />
          </Routes>


        </BrowserRouter>
      </AuthProvider>
    </TooltipProvider>

    
  </QueryClientProvider>
);

export default App;