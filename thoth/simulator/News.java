package thoth.simulator;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class News {
    private float effect;
    private float persistence = 10.0f;
    private String title;
    private String description;
    private float elapsed = 0.0f; // TODO: refactor this for other timesteps than 1sec

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

    public float getInitialEffect() {
        return this.effect;
    }

    /// Returns the measurable effect on stocks.
    public float getEffect() {
        if (this.elapsed >= this.persistence) {
            return 0.0f;
        }
        float t = 1f - (this.elapsed / this.persistence);
        return (float) Math.sqrt(1.0f - Math.pow(t - 1.0f, 2)) * this.effect;
    }

    private float lerp(float a, float b, float factor) {
        return a * factor + b * (1f - factor);
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
        new News("Ferrari ouvre une salle d’exposition", "Une nouvelle vitrine s’installe en Italie.", +1.0f),
        new News("Apple visée par une enquête antitrust", "La Commission européenne ouvre une procédure sur l’App Store et pourrait imposer des changements majeurs au modèle économique.", -16.0f),
        new News("Apple mise sur l’IA générative", "Apple intègre une IA avancée dans iOS et annonce des partenariats cloud, rassurant les marchés sur sa capacité d’innovation.", +18.0f),
        new News("Microsoft rate un contrat gouvernemental", "Un important appel d’offres cloud aux États-Unis est remporté par un concurrent, pesant sur les ambitions de croissance d’Azure.", -12.0f),
        new News("Microsoft dévoile une suite IA pour entreprises", "Une nouvelle offre d’abonnements IA augmente fortement la valeur par utilisateur et doperait la rentabilité à moyen terme.", +20.0f),
        new News("Google perd un procès sur la publicité", "Un tribunal impose à Google de modifier ses pratiques publicitaires et menace une partie de ses revenus ciblés.", -14.0f),
        new News("Google réussit une avancée en IA quantique", "Une percée dans le calcul quantique appliqué à l’IA ouvre la voie à de nouveaux services à haute valeur ajoutée.", +22.0f),
        new News("Amazon confronté à une grève massive", "Des entrepôts stratégiques en Europe sont bloqués pendant plusieurs jours, perturbant les livraisons.", -18.0f),
        new News("Amazon renforce sa rentabilité cloud", "AWS augmente ses marges grâce à l’optimisation énergétique de ses data centers et à des contrats à long terme.", +17.0f),
        new News("Meta menacée d’interdiction d’app en Europe", "Un régulateur européen envisage de suspendre certaines fonctionnalités de Meta pour non‑respect du RGPD.", -15.0f),
        new News("Meta monétise mieux ses Reels", "Une nouvelle formule publicitaire améliore le revenu par minute de visionnage sur Instagram et Facebook.", +13.0f),
        new News("Tesla visée par un scandale de sécurité", "Une enquête révèle des accidents non déclarés liés à l’autopilot, entraînant un risque de rappel massif.", -26.0f),
        new News("Tesla ouvre une gigafactory en Inde", "Une nouvelle usine réduit les coûts de production et permet d’attaquer un marché en forte croissance.", +24.0f),
        new News("Netflix perd un gros catalogue", "Un studio récupère les droits de ses séries phares, entraînant une baisse potentielle d’abonnés.", -16.0f),
        new News("Netflix réussit sa formule avec publicité", "La déclinaison avec pubs devient plus rentable que l’offre standard et séduit de nouveaux abonnés sensibles au prix.", +19.0f),
        new News("Disney échoue sur un film majeur", "Un blockbuster très attendu fait un flop au box‑office, fragilisant la stratégie cinéma du groupe.", -12.0f),
        new News("Disney+ atteint enfin la rentabilité", "La branche streaming du groupe passe dans le vert, ce qui rassure les investisseurs sur le modèle numérique.", +18.0f),
        new News("LVMH souffre d’un ralentissement chinois", "Les ventes de produits de luxe reculent en Asie, faisant craindre un essoufflement de la croissance.", -14.0f),
        new News("LVMH renforce ses marges", "La hausse des prix et le succès des produits iconiques améliorent la rentabilité du secteur mode et maroquinerie.", +16.0f),
        new News("Hermès confronté à des tensions sociales", "Des mouvements de grève dans plusieurs ateliers menacent la production à court terme.", -8.0f),
        new News("Hermès affiche un carnet de commandes record", "La demande dépasse largement l’offre, garantissant plusieurs années de croissance très visible.", +14.0f),
        new News("BNP Paribas exposée à un défaut souverain", "Une forte exposition obligataire à un pays émergent en crise inquiète les marchés.", -20.0f),
        new News("BNP Paribas surprend avec ses résultats", "La banque publie des bénéfices largement supérieurs aux attentes grâce à ses activités de marché.", +15.0f),
        new News("Crédit Agricole pénalisé par le crédit immobilier", "La montée des défauts de remboursement pèse sur la rentabilité en France.", -13.0f),
        new News("Crédit Agricole accélère dans l’assurance", "La filiale d’assurance affiche une forte croissance et stabilise les revenus du groupe.", +11.0f),
        new News("Airbus subit une série d’annulations", "Plusieurs compagnies annulent ou reportent des commandes de long‑courriers.", -17.0f),
        new News("Airbus augmente son rythme de production", "La montée en cadence des appareils moyen‑courrier améliore la visibilité du carnet de commandes.", +19.0f),
        new News("Boeing de nouveau cloué au sol", "Un nouveau problème technique conduit à l’immobilisation temporaire d’un modèle clé.", -22.0f),
        new News("Boeing sécurise un contrat militaire stratégique", "Un accord pluriannuel avec le Pentagone stabilise les flux de revenus.", +14.0f),
        new News("Volkswagen confronté à un malware industriel", "Une cyberattaque perturbe plusieurs usines européennes pendant plusieurs jours.", -16.0f),
        new News("Volkswagen réduit ses coûts fixes", "Un plan d’économies massif améliore la marge opérationnelle sur le segment électrique.", +12.0f),
        new News("BMW rappel massif sur un moteur", "Un défaut de conception impose un rappel mondial de plusieurs modèles récents.", -18.0f),
        new News("BMW réussit sur le haut de gamme électrique", "Les ventes de berlines électriques premium dépassent les attentes en Europe et en Chine.", +15.0f),
        new News("Mercedes sanctionnée pour émissions truquées", "Une enquête révèle des logiciels non conformes sur certains moteurs diesel.", -20.0f),
        new News("Mercedes développe un logiciel propriétaire", "Le constructeur propose une plateforme logicielle maison vendue par abonnement.", +17.0f),
        new News("Ferrari critiquée pour ses délais", "Les temps d’attente très longs irritent une partie de la clientèle la plus fidèle.", -7.0f),
        new News("Ferrari lance un modèle hyper‑luxe limité", "Une série ultra limitée est vendue avant même sa présentation officielle.", +18.0f),
        new News("TotalEnergies fait face à une taxe exceptionnelle", "Un nouveau prélèvement sur les super‑profits énergétiques réduit les bénéfices attendus.", -19.0f),
        new News("TotalEnergies réussit un virage solaire", "Une grande ferme solaire en Afrique commence à générer des flux de trésorerie significatifs.", +16.0f),
        new News("Shell visée par un procès climatique", "Plusieurs ONG attaquent Shell pour non‑respect de ses engagements de réduction d’émissions.", -17.0f),
        new News("Shell cède des actifs fossiles", "La vente d’actifs pétroliers à faible marge renforce le bilan et la stratégie de transition.", +13.0f),
        new News("EDF confronté à un incident sur le réseau", "Une panne majeure entraîne une coupure d’électricité dans plusieurs régions françaises.", -18.0f),
        new News("EDF bénéficie d’un soutien tarifaire", "Un nouveau cadre réglementaire sécurise ses revenus sur plusieurs années.", +12.0f),
        new News("Engie subit un hiver doux", "La demande en gaz est plus faible que prévu, réduisant les revenus de la saison hivernale.", -9.0f),
        new News("Engie signe un méga‑contrat d’énergie verte", "Un accord à long terme avec un consortium industriel sécurise des volumes renouvelables importants.", +14.0f),
        new News("Orange s’expose à une fuite de données", "Une brèche de sécurité touche des millions de clients en Europe.", -21.0f),
        new News("Orange réussit son plan fibre", "Le taux de conversion fibre dépasse les prévisions et augmente l’ARPU.", +13.0f),
        new News("Carrefour pris dans un scandale de fournisseurs", "Des pratiques sociales contestées dans la chaîne d’approvisionnement font polémique.", -11.0f),
        new News("Carrefour renforce ses marques propres", "La montée en puissance des MDD améliore les marges en période d’inflation.", +9.0f),
        new News("Leclerc subit une cyberattaque sur ses drives", "Les commandes en ligne sont perturbées plusieurs jours.", -10.0f),
        new News("Leclerc fidélise avec un programme carburant", "Un nouveau système de remises sur l’essence attire de nombreux nouveaux clients.", +8.0f),
        new News("Ryanair perd un procès sur les conditions de travail", "La compagnie doit revoir certains contrats et faire face à des charges supplémentaires.", -13.0f),
        new News("Ryanair profite d’un boom touristique", "Le trafic dépasse les niveaux d’avant‑crise et les avions tournent à pleine capacité.", +17.0f),
        new News("Air France touché par une nouvelle grève des pilotes", "Des centaines de vols sont annulés, fragilisant l’image de fiabilité.", -14.0f),
        new News("Air France améliore sa classe affaires", "Une nouvelle cabine premium séduit la clientèle corporate à forte contribution.", +10.0f),
        new News("Uber confronté à une régulation plus stricte en Europe", "Plusieurs villes imposent des quotas et des licences plus coûteuses.", -16.0f),
        new News("Uber atteint la rentabilité sur un trimestre", "Les activités de mobilité et de livraison combinées passent dans le vert.", +18.0f),
        new News("Spotify perd l’exclusivité d’un podcast star", "Le départ d’un contenu phare réduit l’attractivité de la plateforme.", -9.0f),
        new News("Spotify lance une offre Hi‑Fi très rentable", "Un supplément payant pour audio haute qualité est adopté par une partie des abonnés.", +11.0f),
        new News("TikTok menacé d’une interdiction dans un grand pays", "Un gouvernement prépare une loi pouvant bannir l’application du territoire.", -20.0f),
        new News("TikTok diversifie ses revenus e‑commerce", "Les fonctionnalités d’achats intégrés explosent dans plusieurs marchés asiatiques.", +16.0f),
        new News("Bitcoin subit une correction brutale", "Un durcissement réglementaire fait chuter le prix après une forte hausse précédente.", -28.0f),
        new News("Bitcoin adopté par un grand gestionnaire d’actifs", "Le lancement d’un ETF spot génère des flux d’investissement massifs.", +26.0f),
        new News("Ethereum reste bloqué par des frais élevés", "Une congestion du réseau renchérit les transactions et pénalise certains usages.", -15.0f),
        new News("Ethereum réussit une mise à jour majeure", "Une upgrade réduit les coûts et améliore la vitesse des transactions sur le réseau.", +20.0f),
        new News("Apple investit dans des puces maison", "Apple annonce une nouvelle génération de puces plus efficaces pour Mac et iPhone, promettant des marges améliorées.", +17.0f),
        new News("Apple rappel de chargeurs défectueux", "Des millions de chargeurs sont rappelés après des risques de surchauffe signalés dans plusieurs pays.", -13.0f),
        new News("Microsoft subit une panne mondiale de cloud", "Une interruption d’Azure affecte de grandes entreprises pendant plusieurs heures, relançant la question de la dépendance au cloud.", -18.0f),
        new News("Microsoft signe un contrat géant avec un État", "Un gouvernement adopte massivement les solutions cloud et IA de Microsoft pour son administration.", +19.0f),
        new News("Google ferme un service emblématique", "Google annonce la fermeture d’un service historique peu rentable, suscitant des critiques d’utilisateurs fidèles.", -6.0f),
        new News("Google monétise mieux YouTube Shorts", "Les revenus publicitaires de Shorts dépassent les attentes et relancent la croissance du segment vidéo mobile.", +14.0f),
        new News("Amazon pointé du doigt sur l’empreinte carbone", "Un rapport environnemental épingle les émissions liées à la logistique d’Amazon.", -10.0f),
        new News("Amazon déploie des entrepôts robotisés", "Une automatisation accrue réduit les coûts et accélère les livraisons sur les principaux marchés.", +16.0f),
        new News("Meta perd un procès sur la protection des données", "Une lourde amende et des restrictions sur l’usage des données ciblées sont imposées.", -17.0f),
        new News("Meta réussit son pari dans la VR", "Les ventes de casques et les abonnements à la plateforme immersive dépassent les projections.", +18.0f),
        new News("Tesla réduit ses prix en Europe", "Des baisses de tarifs pèsent sur les marges mais stimulent la demande.", -7.0f),
        new News("Tesla dévoile un modèle d’entrée de gamme", "Un véhicule électrique moins cher ouvre un nouveau segment de marché.", +21.0f),
        new News("Netflix sanctionné pour partage de données", "Une autorité de régulation découvre des pratiques jugées opaques dans la gestion des données utilisateurs.", -11.0f),
        new News("Netflix s’implante dans le sport en direct", "Le lancement de droits sportifs premium attire un nouveau public et de nouveaux annonceurs.", +20.0f),
        new News("Disney réduit la voilure dans ses parcs", "Des fermetures partielles et une baisse de fréquentation pénalisent l’activité parcs et loisirs.", -9.0f),
        new News("Disney valorise ses licences en jeux vidéo", "Une série de jeux à succès basés sur ses franchises relance les revenus de licences.", +13.0f),
        new News("LVMH cible des économies de coûts", "Un plan de rationalisation inquiète sur la dynamique de croissance à court terme.", -8.0f),
        new News("LVMH renforce sa présence en ligne", "Les ventes e‑commerce haut de gamme progressent plus vite que le retail physique.", +12.0f),
        new News("Hermès critiqué pour son manque de transparence ESG", "Des investisseurs demandent plus d’engagement sur les enjeux climatiques.", -5.0f),
        new News("Hermès ouvre une école de savoir-faire", "Une initiative de formation sécurise les compétences artisanales et la capacité de production future.", +9.0f),
        new News("BNP Paribas vend une filiale non stratégique", "La cession allège le bilan mais réduit légèrement la diversification des revenus.", -4.0f),
        new News("BNP Paribas renforce son activité de gestion d’actifs", "Des encours en forte hausse augmentent les commissions récurrentes du groupe.", +13.0f),
        new News("Crédit Agricole touché par une hausse des créances douteuses", "La montée des impayés dans le crédit à la consommation pèse sur les résultats.", -12.0f),
        new News("Crédit Agricole développe la banque en ligne", "Une offre 100% digitale attire une clientèle plus jeune à moindre coût.", +10.0f),
        new News("Airbus subit des tensions sur les fournisseurs", "Des retards de livraison de pièces ralentissent la montée en cadence des avions.", -9.0f),
        new News("Airbus développe un avion à hydrogène", "Un prototype crédible d’avion décarboné attire des financements publics et privés.", +18.0f),
        new News("Boeing perd une commande face à Airbus", "Une grande compagnie renonce à Boeing pour sa prochaine flotte moyen‑courrier.", -14.0f),
        new News("Boeing améliore la qualité de production", "Un plan de contrôle qualité rassure les régulateurs et les compagnies aériennes.", +11.0f),
        new News("Volkswagen retardé sur un logiciel de conduite autonome", "Des bugs importants repoussent la mise sur le marché de véhicules clés.", -13.0f),
        new News("Volkswagen construit une usine de batteries en Europe", "Une nouvelle usine sécurise l’approvisionnement pour ses modèles électriques.", +15.0f),
        new News("BMW affronte une polémique sur le sourcing de matières premières", "Des ONG dénoncent les conditions d’extraction de certains métaux utilisés.", -10.0f),
        new News("BMW déploie des abonnements logiciels dans ses véhicules", "De nouveaux services connectés améliorent le revenu par véhicule vendu.", +12.0f),
        new News("Mercedes pert un gros marché de flottes", "Une grande entreprise choisit un concurrent pour renouveler ses véhicules de société.", -9.0f),
        new News("Mercedes mise sur la conduite assistée haut de gamme", "Un système avancé de pilotage sur autoroute devient un argument décisif pour les ventes.", +14.0f),
        new News("Ferrari confronté à des contraintes environnementales", "Des normes plus strictes menacent la production de moteurs thermiques emblématiques.", -8.0f),
        new News("Ferrari annonce des résultats records", "Des marges exceptionnelles et une demande soutenue font bondir le titre en bourse.", +22.0f),
        new News("TotalEnergies touché par une marée noire", "Un incident environnemental majeur entraîne des coûts de nettoyage et des risques juridiques.", -23.0f),
        new News("TotalEnergies étend son portefeuille d’éolien offshore", "De nouveaux projets sécurisent des revenus de long terme dans les renouvelables.", +15.0f),
        new News("Shell peine à vendre certains actifs fossiles", "Des acheteurs se désengagent, laissant des actifs potentiellement échoués au bilan.", -12.0f),
        new News("Shell conclut des contrats de GNL sur 20 ans", "Des accords à long terme stabilisent les flux de trésorerie malgré la volatilité des prix.", +14.0f),
        new News("EDF fait face à de nouvelles réparations nucléaires", "Des contrôles supplémentaires imposent des arrêts prolongés de réacteurs.", -16.0f),
        new News("EDF bénéficie d’une hausse des prix régulés", "Une révision tarifaire améliore ses perspectives de revenus en France.", +11.0f),
        new News("Engie vend une participation dans un gazoduc", "La cession réduit l’exposition aux énergies fossiles mais diminue un revenu stable.", -5.0f),
        new News("Engie accélère dans le stockage d’énergie", "Des projets de batteries industrielles ouvrent un relais de croissance rentable.", +13.0f),
        new News("Orange perd un contrat d’infogérance", "Un grand client bascule vers un concurrent pour la gestion de ses réseaux.", -7.0f),
        new News("Orange lance une offre de cybersécurité managée", "Une nouvelle gamme de services sécurisés attire les entreprises exposées aux risques numériques.", +12.0f),
        new News("Carrefour critiqué sur la shrinkflation", "La taille de certains produits baisse sans baisse de prix, irritant les consommateurs.", -8.0f),
        new News("Carrefour développe des drives urbains compacts", "Un nouveau format de retrait rapide séduit les citadins pressés.", +9.0f),
        new News("Leclerc fait face à une pénurie de certains produits", "Des tensions d’approvisionnement vident temporairement des rayons clés.", -6.0f),
        new News("Leclerc négocie fermement avec les industriels", "Des baisses de tarifs obtenues sur des marques nationales améliorent les marges.", +7.0f),
        new News("Ryanair confronté à une hausse du carburant", "L’augmentation des prix du kérosène réduit sa marge malgré un trafic solide.", -11.0f),
        new News("Ryanair ouvre de nouvelles bases en Europe de l’Est", "Une expansion dans des aéroports peu saturés dope la croissance à coûts maîtrisés.", +16.0f),
        new News("Air France visée par une enquête sur les aides d’État", "Bruxelles questionne la conformité des soutiens publics reçus pendant la crise.", -10.0f),
        new News("Air France modernise sa flotte avec des avions plus sobres", "La baisse de consommation améliore les coûts et l’image environnementale.", +13.0f),
        new News("Uber critiqué pour l’algorithme de tarification", "Des hausses de prix jugées abusives en période de crise provoquent un bad buzz.", -9.0f),
        new News("Uber Eats signe avec une grande chaîne de restaurants", "Un partenariat exclusif renforce l’attractivité de la plateforme de livraison.", +15.0f),
        new News("Spotify attaqué pour rémunération insuffisante des artistes", "Une campagne d’artistes met la pression sur le modèle économique.", -7.0f),
        new News("Spotify ajoute un onglet IA de création de playlists", "Une personnalisation avancée augmente le temps d’écoute moyen par utilisateur.", +10.0f),
        new News("TikTok accusé d’influence politique", "Des enquêtes pointent un risque de manipulation de l’opinion sur la plateforme.", -14.0f),
        new News("TikTok conclut un accord avec les régulateurs", "Des mesures de transparence supplémentaires évitent une interdiction pure et simple.", +12.0f),
        new News("Bitcoin menacé par une nouvelle taxe sur les plus-values", "Plusieurs pays envisagent une fiscalité plus lourde sur les gains crypto.", -19.0f),
        new News("Bitcoin devient un moyen de paiement accepté par un géant du e-commerce", "Une grande plateforme annonce l’acceptation directe des paiements en BTC.", +24.0f),
        new News("Ethereum visé par une faille sur un protocole DeFi majeur", "Un piratage sur un protocole très utilisé fait chuter la confiance temporairement.", -17.0f),
        new News("Ethereum attire des institutions via des solutions de staking régulé", "Des produits conformes aux normes financières permettent aux investisseurs institutionnels d’entrer sur le marché.", +19.0f)

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
