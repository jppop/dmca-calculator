# Règles de calculs des charges sociales

## Règles générales

Les règles de calculs des charges sociales (cotisations, taxes et contributions) appliqués aux chiffres d'affaires des Micro-entrepreneur.

- Les **charges sociales** sont calculées en appliquant un pourcentage (**taux**) au chiffre d'affaires.
- Le calcul du montant des charges sociales dépend de la **nature du chiffre d'affaires** (BICP, BICV, BNC ou LMTC),
  de la nature de l'activité principale de l'AE : artisanale, commerciale, libérale ou libéral_nr (non réglementée)
  et de certaines options (par exemple, prélèvement à la source des impôts sur le revenu ou la contribution à la formation professionnelle).
- Les types d'activités sont également regroupées en **natures d'activité**.
  Elles sont codées "NA99" (99 représente un nombre à 2 chiffres).
  Ces codes sont utilisés par des société tierces (des plateformes numériques utilisant des AE).
  Chaque nature d'activité correspond à une des 4 natures de CA.
- Les charges sociales sont regroupées en 4 catégories : COTISATION, TAXE, CFP et CCICMA.</br>
  La catégorie COTISATION regroupe les cotisations sociales ;
  TAXE, l'impôt sur le revenu ;
  CFP, la contribution à la formation professionnelle ; 
  CCICMA, des taxes pour les organismes professionnels (Chambre d'Industrie et du Commerce et Chambre de Métiers et de l'Artisanat).
- Une charge sociale peut comporter plusieurs "CTP" (risque couvert par la charge).
- A chaque CTP correspond un taux applicable au chiffre d'affaires.
- Les valeurs des taux évoluent dans le temps (ils ont une période d'application).
- La somme des taux de chaque CTP donne le taux appliqué au CA
- Les règles de calcul évoluent dans le temps selon la réglementation en vigueur et peuvent varier selon le lieu
  d'activité.
- Attention à ne pas confondre la nature de l'activité et l'activité principale de l'AE
  (artisanale, commerciale ou libérale). Un artisan, par exemple, peut exercer plusieurs activités
  (ventes/commerciale et non commerciale).

## Modélisation

### Les règles de calcul

Les différentes versions des règles de calcul sont regroupées en **feuille de calcul**.
Chaque feuille est composée ainsi :
- Un **identifiant unique**
- La **Période d'application**
  La période définit la date à laquelle la feuille s'applique et, éventuellement quand elle ne s'applique plus (pas de date de fin si la feuille est la dernière la plus récente)
- La **région** où s'applique la feuille : métro (France métropolitaine hors Alsace et Lorraine),
  DROM (Départements et régions d'outre-mer hors Mayotte) et Mayotte.
- Les règles de sélection des CTP applicables en fonctions des données d'entrées (contexte de calcul lié à l'AE).
  Chaque feuille possède 4 types de règles permettant de obtenir, pour une nature d'activité (BICP, BICV, BNC et LMTC)
  la liste des CTP pour chacune des 4 catégories de charges sociales : COTISATION, TAXE, CFP et CCICMA.

Les méthodes associées aux feuilles de calcul permettent de :
- déterminer si la feuille est applicable au contexte de calcul.
- de fournir, pour une nature d'activité et une date d'activité données,
  la liste des CTP applicables en fonction du contexte de calcul.

### Contexte de calcul

Le contexte de calcul permet de sélectionner la feuille de calcul applicable.

Il est défini par :

- l'activité principale de l'AE (artisanale, commerciale, libérale ou libéral_nr)
- La nature de l'activité (BICP, BICV, BNC ou LMTC)
- Des options de calcul spécifiques.
  Par exemple, l'option de prélèvement à la source des impôt sur le revenu.
- D'éventuelle exonérations.
  Chaque exonération est identifiée par un type et une période d'application (date de début et fin).

## Vocabulaire

**Micro-entrepreneur**
: Le statut de micro-entrepreneur (ou auto-entrepreneur) permet d'exercer en nom propre. Il s'agit d'une entreprise individuelle qui bénéficie d'un régime fiscal et social simplifié. La création d'une micro-entreprise est plus rapide et présente moins de contraintes que celle d'une société.
Abrégé en AE (ou ME).

**CA**
: Le chiffre d'affaires

**Charges sociales**
: Les contributions, cotisations et taxes sociales prélevées sur les chiffres d'affaires réalisés.

**BICP**
: Bénéfices industriels et commerciaux Prestation.
  Les revenus liés aux prestations de services dans le cadre d'activités de industrielles ou commerciales.

**BICV**
: Bénéfices industriels et commerciaux Ventes.
  Les revenus liés aux ventes dans le cadre d'activités de industrielles ou commerciales.

**BNC**
: Bénéfices Non Commerciaux.
  Les revenus liés aux activités non commerciales (artistiques, soin, etc.).

**LMTC**
: Location de Meublé de Tourisme non Classé.
  Les revenus liés à la location de meublé de tourisme non classé.

**PLF**
: Prélèvement Libératoire Forfaitaire. Option de l'AR du prélèvement forfaitaire des impôt sur le revenu.
