# Brooklyn House pricing
__[Téléchargement du data set](https://www.kaggle.com/tianhwu/brooklynhomes2003to2017/data)__



Pour generer le .jar : 
 * Se mettre à la racine du projet et exécuter la commande :

        mvn clean package
  et le jar generé sera dans le dossier __target__

Pour lancer l'application :
   * Se mettre à la racine du __.jar__  __[BrooklynHousePricing.jar]__
   * l'exécuter en effectuant la commande suivante :
   
    spark-submit --class com.upmc.dar.App  --master local  BrooklynHousePricing.jar input nbr
    
avec 

* **input :** le chemin absolue du fichier csv à analyser 
* **nbr :** le pourcentage du contenu à analyser (valeur comprise entre [1,100])

## Exemple pour évaluer 40% du dataset

    spark-submit --class com.upmc.dar.App  --master local  BrooklynHousePricing.jar 
    /BrooklynHousePricing/files/brooklyn_sales_map.csv 40