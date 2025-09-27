import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { AlertDialog, AlertDialogAction, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { Ban } from "lucide-react";
import { useNavigate } from 'react-router-dom'; // Added useNavigate
import { useTranslation } from 'react-i18next';

interface Monument {
  id: string;
  name: string;
  description: string;
  imageUrl: string;
  blocked: boolean;
}

const dummyMonuments: Monument[] = [
  {
    id: "1",
    name: "Taj Mahal",
    description: "An immense mausoleum of white marble, built in Agra between 1631 and 1648 by order of Mughal emperor Shah Jahan in memory of his favourite wife, Mumtaz Mahal.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/Taj_Mahal_%28Edited%29.jpeg/1280px-Taj_Mahal_%28Edited%29.jpeg",
    blocked: false,
  },
  {
    id: "2",
    name: "Red Fort",
    description: "The Red Fort is a historic fort in the city of Delhi in India. It was the main residence of the Mughal dynasty for nearly 200 years, until 1856.",
    imageUrl: "https://static.toiimg.com/photo/53438383.cms",
    blocked: false,
  },
  {
    id: "3",
    name: "Gateway of India",
    description: "The Gateway of India is an arch-monument built in the early 20th century in the city of Mumbai, India.",
    imageUrl: "https://lh3.googleusercontent.com/gps-cs-s/AC9h4nrV2FyuWA2lZVZeoTmlrGiv3cqVh5vjcfwnUHPyIEkZi7zYuUkaKEkuxgKtkiAgEqBp-U76c3xZNCri1HEnqh86ynZLbp1Jddo-o2_-_p3BnOnli99Igzc4kcIbKSEzG43TIJ4=w270-h312-n-k-no",
    blocked: false,
  },
  {
    id: "4",
    name: "Hawa Mahal",
    description: "The Hawa Mahal is a palace in Jaipur, India, named because it was essentially a high screen wall made of pink and red sandstone, which allowed royal ladies to observe festivals on the street below without being seen.",
    imageUrl: "https://lh3.googleusercontent.com/gps-cs-s/AC9h4nrjcWzZ9LjSZF3laFjEe3GlNEPp5d2R8lPFaOL6DV79RG2D_MXPCEPhBrCSqhvL58pTladvh7umNYrt9HZrMbzr2j8LIOGUJPg7jPLe8YV_8yQr75TcwS5ndA8vKCUc-Y9sDOLwgA=w270-h312-n-k-no",
    blocked: false,
  },
  {
    id: "5",
    name: "Qutub Minar",
    description: "The Qutub Minar is a minaret and 'victory tower' that forms part of the Qutub Complex, a UNESCO World Heritage Site in the Mehrauli area of Delhi, India.",
    imageUrl: "data:image/jpeg;base64,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",
    blocked: false,
  },
];

const TicketBlockage = () => {
  const [monuments, setMonuments] = useState<Monument[]>(dummyMonuments);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedMonumentName, setSelectedMonumentName] = useState<string>("");
  const navigate = useNavigate(); // Initialized navigate
  const { t } = useTranslation();

  const handleBlockTicket = (id: string) => {
    setMonuments((prevMonuments) =>
      prevMonuments.map((monument) =>
        monument.id === id ? { ...monument, blocked: !monument.blocked } : monument
      )
    );
    const monument = monuments.find((m) => m.id === id);
    if (monument) {
      setSelectedMonumentName(monument.name);
      setDialogOpen(true);
    }
  };

  return (
    <div className="min-h-screen bg-background p-6">
      <div className="container mx-auto max-w-6xl space-y-8">
        <div className="flex items-center justify-between text-center"> {/* Added flex container */}
          <div>
            <h1 className="text-3xl md:text-4xl font-bold text-foreground mb-2 bg-gradient-to-r from-golden to-green bg-clip-text text-transparent">{t("ticketBlockage")}</h1>
            <p className="text-muted-foreground">{t("manageTicketAvailability")}</p>
          </div>
          <Button variant="outline" onClick={() => navigate('/authority')}>{t("backToDashboard")}</Button> {/* Added Button */}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {monuments.map((monument) => (
            <Card key={monument.id} className="border-golden/30 overflow-hidden">
              <img src={monument.imageUrl} alt={monument.name} className="w-full h-48 object-cover" />
              <CardHeader className="bg-gradient-to-r from-golden/10 to-green/10 rounded-b-none">
                <CardTitle className="text-foreground">{monument.name}</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-muted-foreground text-sm">{monument.description}</p>
                <Button
                  variant={monument.blocked ? "destructive" : "default"}
                  onClick={() => handleBlockTicket(monument.id)}
                  className={monument.blocked ? "w-full" : "w-full bg-gradient-to-r from-green to-golden hover:from-green-dark hover:to-golden-dark text-white"}
                >
                  <Ban className="w-4 h-4 mr-2" />
                  {monument.blocked ? t("ticketBlocked") : t("blockTicket")}
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>

      <AlertDialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{t("ticketStatusUpdated")}</AlertDialogTitle>
            <AlertDialogDescription>
              {t("ticketStatusUpdatedMessage")}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogAction>{t("ok")}</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default TicketBlockage;
