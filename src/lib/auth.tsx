import { createContext, useContext, useEffect, useState } from 'react';
import { onAuthStateChanged, signOut, User, setPersistence, browserLocalPersistence } from 'firebase/auth';
import { auth, db } from './firebase';
import { doc, getDoc } from 'firebase/firestore';

export type AppUser = (User & { role?: string | null }) | null;

type AuthContextValue = {
  user: AppUser;
  loading: boolean;
  role: string | null;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue>({
  user: null,
  loading: true,
  role: null,
  logout: async () => {},
});

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<AppUser>(null);
  const [role, setRole] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setPersistence(auth, browserLocalPersistence).then(() => {
      const unsub = onAuthStateChanged(auth, async (fbUser) => {
        setUser(fbUser as AppUser);
        if (fbUser) {
          // fetch role from users collection
          const snap = await getDoc(doc(db, 'users', fbUser.uid));
          setRole(snap.exists() ? (snap.data() as any).role ?? null : null);
        } else {
          setRole(null);
        }
        setLoading(false);
      });
      return () => unsub();
    });
  }, []);

  const logout = async () => {
    await signOut(auth);
  };

  return (
    <AuthContext.Provider value={{ user, loading, role, logout }}>
      {children}
    </AuthContext.Provider>
  );
};