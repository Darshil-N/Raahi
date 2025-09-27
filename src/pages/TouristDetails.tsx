import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { db } from '@/lib/firebase';
import { doc, getDoc } from 'firebase/firestore';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { useTranslation } from 'react-i18next';
interface TouristDoc {
  uid: string;
  role: 'tourist';
  name: string;
  idNumber: string;
  age: string;
  gender: string;
  email: string;
  nationality: string;
  state?: string;
  phone: string;
  emergencyContactName: string;
  emergencyContactNumber: string;
  passportNumber?: string;
  visaNumber?: string;
  visaTimeline?: string;
  bloodGroup?: string;
  allergies?: string;
  medicalRecord?: string;
  insuranceAgencyName?: string;
  insuranceId?: string;
  blockchainTxHash?: string | null;
}
const TouristDetails = () => {
  const { id } = useParams();
  const [data, setData] = useState<TouristDoc | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { t } = useTranslation();
  useEffect(() => {
    const load = async () => {
      try {
        if (!id) return;
        const snap = await getDoc(doc(db, 'tourist', id));
        if (!snap.exists()) {
          setError(t('noTouristFound'));
        } else {
          setData(snap.data() as TouristDoc);
        }
      } catch (e: any) {
        setError(e.message ?? t('failedToLoad'));
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id, t]);

  if (loading) return null;
  if (error) return <div className="p-6 text-destructive">{error}</div>;
  if (!data) return <div className="p-6">{t("notFound")}</div>;




  return (
    <div className="min-h-screen bg-background p-6">
      <div className="container mx-auto max-w-3xl space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>{t("touristDetailsTitle")}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-sm">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div><b>{t("nameLabel")}</b> {data.name}</div>
              <div><b>{t("emailLabel")}</b> {data.email}</div>
              <div><b>{t("genderLabel")}</b> {data.gender}</div>
              <div><b>{t("ageLabel")}</b> {data.age}</div>
              <div><b>{t("nationalityLabel")}</b> {data.nationality}</div>
              <div><b>{t("stateLabel")}</b> {data.state || '-'}</div>
              <div><b>{t("phoneLabel")}</b> {data.phone}</div>
              <div><b>{t("idNumberLabel")}</b> {data.idNumber}</div>
            </div>
            <Separator />
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div><b>{t("emergencyContactNameLabel")}</b> {data.emergencyContactName}</div>
              <div><b>{t("emergencyContactNumberLabel")}</b> {data.emergencyContactNumber}</div>
            </div>
            <Separator />
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div><b>{t("bloodGroupLabel")}</b> {data.bloodGroup || '-'}</div>
              <div><b>{t("allergiesLabel")}</b> {data.allergies || '-'}</div>
              <div className="md:col-span-2"><b>{t("medicalRecordLabel")}</b> {data.medicalRecord || '-'}</div>
            </div>
            <Separator />
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div><b>{t("insuranceAgencyLabel")}</b> {data.insuranceAgencyName || '-'}</div>
              <div><b>{t("insuranceIdLabel")}</b> {data.insuranceId || '-'}</div>
            </div>





            <Separator />
            <div>
              <b>{t("blockchainTxHashLabel")}</b>{' '}
              {data.blockchainTxHash ? (
                <a href={`https://www.oklink.com/amoy/tx/${data.blockchainTxHash}`} target="_blank" rel="noreferrer" className="text-golden underline">
                  {data.blockchainTxHash}
                </a>
              ) : (
                '-'
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
export default TouristDetails;