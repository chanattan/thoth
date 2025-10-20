package thoth.simulator;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class News {
    private float effect;
    private String title;
    private String description;
    private boolean used = false;

    /// Initializes a news item.
    public News(String title, String description, float effect) {
        this.title = title;
        this.description = description;
        this.effect = effect;
    }

    /// Returns the title of this news.
    public String getTitle() {
        return this.title;
    }

    /// Returns the description of this news (longer text)
    public String getDescription() {
        return this.description;
    }

    /// Returns the measurable effect on stocks.
    public float getEffect() {
        return this.effect;
    }

    /**
     * Deplete the News' effect.
     */
    public float useEffect() {
        if (!used) {
            this.used = true;
            return getEffect();
        }
        return 0f;
    }

    /*
        Static table of news.
    */
    static final News defined_news[] = {
        new News("BNP Paribas manque de sérieux", "BNP Paribas a perdu des documents administratifs, +20% de risques.", -20.0f),
        new News("Apple annonce un nouvel iPhone", "Apple dévoile un modèle avec plus de performance et d’autonomie.", +15.0f),
        new News("Tesla en difficulté", "Tesla rappelle 200 000 véhicules pour un problème de batterie.", -25.0f),
        new News("Amazon bat des records", "Amazon atteint un chiffre d’affaires historique ce trimestre.", +30.0f),
        new News("Microsoft investit dans l’IA", "Microsoft injecte 10 milliards dans une startup d’intelligence artificielle.", +22.0f),
        new News("Google sanctionné", "Google reçoit une amende record pour abus de position dominante.", -18.0f),
        new News("Facebook change de stratégie", "Meta investit massivement dans la réalité augmentée.", +10.0f),
        new News("Crise énergétique en Europe", "Les prix du gaz atteignent des niveaux records.", -28.0f),
        new News("LVMH explose en bourse", "Le luxe français résiste à la crise et attire les investisseurs.", +26.0f),
        new News("Renault lance un modèle électrique", "Renault dévoile une citadine électrique grand public.", +12.0f),
        new News("Uber accusé de fraude", "Des enquêtes révèlent des pratiques douteuses chez Uber.", -14.0f),
        new News("Airbus signe un contrat géant", "Airbus vend 150 avions à une compagnie asiatique.", +20.0f),
        new News("Boeing confronté à des retards", "Boeing peine à livrer ses avions à temps.", -15.0f),
        new News("Spotify augmente ses abonnements", "Spotify annonce une hausse de prix pour ses abonnés premium.", -5.0f),
        new News("Netflix sort un blockbuster", "Netflix enregistre une audience record avec son nouveau film.", +18.0f),
        new News("Disney ferme un parc", "Disneyland Paris ferme temporairement à cause d’une grève.", -10.0f),
        new News("Shell profite du pétrole", "Shell annonce des bénéfices records grâce à la hausse du pétrole.", +25.0f),
        new News("TotalEnergies critiqué", "TotalEnergies accusé d’aggraver le réchauffement climatique.", -22.0f),
        new News("BNP Paribas investit en Afrique", "BNP Paribas ouvre 50 agences en Afrique de l’Ouest.", +14.0f),
        new News("Crédit Agricole victime d’une cyberattaque", "Des millions de clients concernés par une fuite de données.", -30.0f),
        new News("Twitter limite les publications", "Elon Musk impose des restrictions sur la plateforme.", -12.0f),
        new News("SpaceX réussit un lancement", "SpaceX place 60 satellites en orbite avec succès.", +28.0f),
        new News("NASA repousse une mission", "La mission Artemis est retardée de plusieurs mois.", -8.0f),
        new News("Coca-Cola lance une boisson sans sucre", "Un nouveau produit séduit les jeunes consommateurs.", +10.0f),
        new News("McDonald’s accusé de maltraitance animale", "Des associations pointent du doigt ses fournisseurs.", -18.0f),
        new News("Adidas perd son contrat avec Kanye West", "La marque allemande coupe les ponts après des polémiques.", -15.0f),
        new News("Nike signe avec Mbappé", "Nike prolonge son partenariat exclusif avec le joueur français.", +20.0f),
        new News("Zara ouvre 200 magasins", "Zara continue son expansion mondiale malgré la crise.", +12.0f),
        new News("H&M ferme des boutiques", "H&M réduit son réseau physique pour se concentrer sur le digital.", -10.0f),
        new News("Carrefour augmente ses prix", "Le groupe alimente la polémique sur l’inflation alimentaire.", -14.0f),
        new News("Leclerc gagne des parts de marché", "Leclerc devient le supermarché préféré des Français.", +8.0f),
        new News("EDF fragilisé par ses dettes", "EDF annonce un déficit record lié au nucléaire.", -25.0f),
        new News("Engie mise sur les énergies vertes", "Engie double ses investissements dans le renouvelable.", +18.0f),
        new News("Orange victime d’une panne", "Des millions de clients privés d’Internet en France.", -20.0f),
        new News("Free annonce une offre illimitée", "Free casse les prix avec un forfait mobile inédit.", +15.0f),
        new News("Bouygues Telecom perd des abonnés", "Bouygues voit sa base client reculer de 5%.", -12.0f),
        new News("SFR condamné", "SFR doit indemniser ses clients après des pratiques abusives.", -18.0f),
        new News("La SNCF en grève", "Des perturbations majeures attendues sur les rails.", -20.0f),
        new News("Air France sauvé par l’État", "Le gouvernement injecte 7 milliards pour éviter la faillite.", +10.0f),
        new News("EasyJet annule des vols", "La compagnie low-cost subit une pénurie de personnel.", -8.0f),
        new News("Ryanair augmente ses bénéfices", "La compagnie aérienne low-cost affiche des profits records.", +22.0f),
        new News("Volkswagen investit dans l’électrique", "Le constructeur allemand annonce un plan de 50 milliards.", +18.0f),
        new News("BMW en baisse", "BMW annonce des ventes en recul sur le marché chinois.", -12.0f),
        new News("Mercedes innove", "Mercedes lance une berline 100% électrique haut de gamme.", +20.0f),
        new News("Ferrari cartonne en bourse", "Les ventes de Ferrari dépassent toutes les attentes.", +25.0f),
        new News("Peugeot confronté à des rappels", "Des milliers de véhicules rappelés pour un défaut technique.", -15.0f),
        new News("Decathlon lance un vélo connecté", "Un nouveau vélo électrique connecté séduit les citadins.", +12.0f),
        new News("IKEA victime de retard", "Des problèmes logistiques entraînent des délais de livraison.", -10.0f),
        new News("AccorHotels reprend des couleurs", "Le secteur hôtelier redémarre après la pandémie.", +15.0f),
        new News("Booking.com sanctionné", "La plateforme condamnée pour pratiques anticoncurrentielles.", -18.0f),
        new News("Tripadvisor profite du tourisme", "Les voyages repartent et boostent l’activité.", +20.0f),
        new News("Twitter supprime des comptes", "La plateforme suspend des milliers de faux profils.", +10.0f),
        new News("LinkedIn en forte croissance", "Le réseau professionnel gagne 30 millions de membres.", +18.0f),
        new News("Snapchat perd en popularité", "Snapchat enregistre une baisse d’utilisateurs actifs.", -12.0f),
        new News("TikTok inquiète les autorités", "Les gouvernements s’inquiètent de la collecte de données.", -15.0f),
        new News("YouTube paie mieux les créateurs", "La plateforme améliore son programme de monétisation.", +20.0f),
        new News("Sony annonce une nouvelle PlayStation", "Une console plus puissante et économe en énergie.", +25.0f),
        new News("Nintendo en retard", "Nintendo repousse la sortie d’un jeu très attendu.", -8.0f),
        new News("Ubisoft en crise", "Ubisoft annule plusieurs projets de jeux vidéo.", -15.0f),
        new News("EA Sports triomphe", "Le nouveau FIFA bat des records de ventes.", +18.0f),
        new News("Activision racheté", "Microsoft finalise l’acquisition d’Activision Blizzard.", +22.0f),
        new News("Sony Music signe un nouvel artiste", "Une star montante signe un contrat exclusif.", +10.0f),
        new News("Warner Bros accuse des pertes", "Le studio enregistre une baisse des revenus cinéma.", -18.0f),
        new News("Paramount+ gagne des abonnés", "La plateforme de streaming attire de nouveaux clients.", +15.0f),
        new News("HBO Max supprime des séries", "Des contenus disparaissent du catalogue pour réduire les coûts.", -12.0f),
        new News("Canal+ investit dans le sport", "Canal+ acquiert de nouveaux droits TV exclusifs.", +20.0f),
        new News("TF1 en baisse d’audience", "La première chaîne française perd du terrain face au streaming.", -10.0f),
        new News("M6 progresse", "M6 gagne des parts de marché en publicité.", +12.0f),
        new News("France Télévisions critiqué", "Des polémiques autour de ses financements publics.", -8.0f),
        new News("Arte en hausse", "La chaîne culturelle séduit un public plus jeune.", +10.0f),
        new News("Santé : nouvelle pandémie redoutée", "L’OMS alerte sur un virus émergent.", -28.0f),
        new News("Vaccin efficace à 95%", "Un nouveau vaccin montre des résultats encourageants.", +30.0f),
        new News("Hôpital en crise", "Les urgences débordées à cause du manque de personnel.", -25.0f),
        new News("Nouvelle technologie médicale", "Un implant révolutionnaire sauve des vies.", +22.0f),
        new News("Sport : PSG éliminé", "Le Paris Saint-Germain échoue en Ligue des Champions.", -18.0f),
        new News("OM en finale", "L’Olympique de Marseille atteint la finale européenne.", +15.0f),
        new News("Mbappé blessé", "L’attaquant français indisponible plusieurs semaines.", -12.0f),
        new News("Tour de France : victoire française", "Un coureur français gagne une étape mythique.", +20.0f),
        new News("JO Paris 2024 : retards", "Des chantiers accusent un retard inquiétant.", -10.0f),
        new News("Climat : vague de chaleur", "Une canicule historique touche l’Europe.", -22.0f),
        new News("Energies renouvelables en hausse", "L’énergie solaire dépasse un nouveau record.", +18.0f),
        new News("Pollution record", "Les villes européennes dépassent les seuils de pollution.", -15.0f),
        new News("Transition écologique", "L’UE vote un plan ambitieux pour le climat.", +20.0f),
        new News("Bitcoin en hausse", "Le Bitcoin franchit la barre des 100 000 dollars.", +30.0f),
        new News("Ethereum en baisse", "L’Ethereum perd 15% en une journée.", -18.0f),
        new News("Banque centrale inquiète", "La FED annonce une hausse brutale des taux d’intérêt.", -20.0f),
        new News("Nouveaux record en bourse", "Le CAC 40 atteint un niveau historique.", +25.0f),
        new News("Crise immobilière", "Les prix de l’immobilier chutent fortement.", -15.0f),
        new News("Investissement vert", "Les fonds écologiques séduisent les investisseurs.", +18.0f),
        new News("Start-up française lève 50M€", "Une pépite de la French Tech attire des capitaux.", +20.0f),
        new News("Licornes en difficulté", "Plusieurs start-ups licornes annoncent des licenciements.", -12.0f),
        new News("Chômage en baisse", "Le taux de chômage atteint son plus bas niveau depuis 20 ans.", +15.0f),
        new News("Inflation persistante", "Les ménages souffrent de la hausse des prix.", -18.0f),
        new News("Croissance mondiale revue à la baisse", "Le FMI revoit ses prévisions économiques.", -10.0f),
        new News("Commerce international en hausse", "Les échanges mondiaux reprennent des couleurs.", +12.0f),


        new News("BNP Paribas réorganise ses services", "Le groupe annonce un ajustement interne de son département conformité.", -2.0f),
        new News("Société Générale ouvre une nouvelle antenne", "La banque installe une petite succursale dans une région rurale.", +1.0f),
        new News("Orange expérimente une nouvelle offre", "Un projet pilote est lancé discrètement dans deux villes.", +0.5f),
        new News("EDF reporte un entretien technique", "Une centrale nucléaire verra ses opérations de maintenance décalées.", -1.5f),
        new News("Air France modifie son programme de vols", "Quelques lignes sont ajustées sur la période estivale.", -0.5f),
        new News("Carrefour teste un nouveau concept", "Un magasin expérimental ouvre en banlieue parisienne.", +1.0f),
        new News("Renault change un fournisseur", "Une nouvelle entreprise rejoint la chaîne de production.", 0.0f),
        new News("Stellantis annonce un partenariat local", "Un accord est signé avec une municipalité italienne.", +0.5f),
        new News("TotalEnergies change son siège régional", "Le siège régional sera déplacé dans une autre ville.", -0.5f),
        new News("Engie prolonge un contrat de maintenance", "Un accord est signé pour la gestion technique de ses infrastructures.", +0.2f),
        new News("BNP Paribas modifie ses horaires", "Certains guichets fermeront désormais plus tôt.", -0.2f),
        new News("La SNCF revoit ses plannings", "Les horaires des TGV sont légèrement ajustés.", -0.3f),
        new News("Uber introduit une nouvelle option", "Les passagers peuvent choisir un niveau sonore réduit en voiture.", +0.5f),
        new News("Tesla ajuste sa chaîne logistique", "Certains composants seront importés depuis un autre pays.", -0.8f),
        new News("Microsoft change sa politique interne", "Un nouveau protocole est appliqué pour les employés.", +0.3f),
        new News("Google met à jour ses serveurs", "Une opération de modernisation est prévue la semaine prochaine.", +0.1f),
        new News("Meta introduit une nouvelle charte", "Un document interne met à jour les règles d’utilisation.", 0.0f),
        new News("Amazon modifie son entrepôt", "Un site logistique ferme temporairement pour travaux.", -0.5f),
        new News("Apple ajuste sa boutique en ligne", "De nouvelles conditions de livraison sont publiées discrètement.", -0.2f),
        new News("LVMH ouvre un atelier", "Un atelier de maroquinerie est inauguré dans le sud-ouest de la France.", +0.8f),
        new News("Hermès agrandit une usine", "Un site de production est légèrement étendu.", +1.0f),
        new News("Zara modifie ses collections", "Des articles disparaissent progressivement des rayons.", -0.5f),
        new News("H&M change de fournisseur textile", "Une partie de la production sera réalisée ailleurs.", -0.3f),
        new News("Leclerc introduit un nouveau service", "Un test de retrait automatisé est mis en place dans deux magasins.", +0.7f),
        new News("Auchan réduit certains rayons", "Une restructuration modifie l’offre en magasin.", -0.4f),
        new News("Spotify ajuste son algorithme", "Les recommandations musicales sont modifiées discrètement.", +0.5f),
        new News("Netflix teste une nouvelle interface", "Un design légèrement différent apparaît pour certains utilisateurs.", +0.2f),
        new News("Disney change ses horaires", "Les parcs ajustent leurs heures d’ouverture.", -0.3f),
        new News("Canal+ réorganise sa grille", "Certains programmes changent d’horaire sans communication majeure.", 0.0f),
        new News("TF1 modifie son JT", "Le journal télévisé change de durée de quelques minutes.", +0.1f),
        new News("M6 teste une nouvelle émission", "Un programme pilote est diffusé tard le soir.", 0.0f),
        new News("Arte publie une série documentaire", "Une mini-série discrète sort en streaming.", +0.2f),
        new News("Sony ajuste sa production", "La cadence de production est légèrement réduite.", -0.5f),
        new News("Nintendo met à jour ses services en ligne", "Un correctif technique est déployé sans annonce.", +0.1f),
        new News("Ubisoft reporte une réunion", "Un événement interne est repoussé d’un mois.", -0.2f),
        new News("EA modifie son service client", "Les délais de réponse changent légèrement.", 0.0f),
        new News("Warner Bros change de distributeur", "Un partenariat est signé avec une société de diffusion régionale.", -0.5f),
        new News("Paramount ajuste son catalogue", "Quelques films disparaissent du service de streaming.", -0.4f),
        new News("Booking.com modifie ses conditions", "Les conditions générales évoluent discrètement.", -0.3f),
        new News("Tripadvisor met à jour son système", "De nouvelles règles d’avis apparaissent.", +0.2f),
        new News("Airbnb introduit une nouvelle taxe", "Des frais supplémentaires apparaissent dans certaines villes.", -1.0f),
        new News("IKEA change sa politique de retour", "Les délais de retour sont réduits de quelques jours.", -0.7f),
        new News("Decathlon déplace une boutique", "Un magasin ferme pour réouverture dans une autre zone.", 0.0f),
        new News("AccorHotels modifie son programme fidélité", "Quelques avantages sont modifiés discrètement.", -0.4f),
        new News("Hyatt ouvre une petite annexe", "Un hôtel annexe est inauguré dans une grande ville.", +0.5f),
        new News("Peugeot lance une édition limitée", "Un modèle spécial est produit en petite quantité.", +0.8f),
        new News("BMW met à jour son service numérique", "Les logiciels embarqués reçoivent une nouvelle interface.", +0.4f),
        new News("Volkswagen ajuste ses campagnes publicitaires", "Un nouveau slogan est testé localement.", 0.0f),
        new News("Mercedes change son logo interne", "Une version modernisée est introduite dans ses documents.", 0.0f),
        new News("Ferrari ouvre une salle d’exposition", "Une nouvelle vitrine s’installe en Italie.", +1.0f)
    };

    /*
        Generates a pool of n different news.
    */
    public static ArrayList<News> generateNews(int n) {
        ArrayList<News> news = new ArrayList<News>();
        
        // Select a random set of news.
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for(int i = 0; i < defined_news.length; i++) {
            indices.add(i);
        }

        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            int index = rand.nextInt(indices.size());
            int elem = indices.remove(index);
            news.add(defined_news[elem]); // No need to copy as the object is not changed.
        }
        return news;
    }

    /**
     * Returns if the given news correspond to a given fund name.
     */
    public boolean correspondsTo(String fundName) {
        return this.getTitle().toLowerCase().contains(fundName.toLowerCase());
    }

    /**
     * Yields one news for a given fund name.
     */
    public static News yieldNew(String fundName) {
        Random r = new Random();
        ArrayList<News> correspondings = new ArrayList<News>();
        for (News n : defined_news) {
            if (n.correspondsTo(fundName)) {
                correspondings.add(n);
            }
        }
        if (!correspondings.isEmpty()) {
            return correspondings.get(r.nextInt(0, correspondings.size() - 1));
        }
        return null;
    }

    public static void test(String[] args) {
        System.out.println(generateNews(10).stream().map(News::getTitle).collect(Collectors.toList()));
    }
}
