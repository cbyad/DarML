# Brooklyn House pricing

Pour lancer l'application :
   * Se mettre à la racine du __.jar__  __[BrooklynHousePricing.jar]__
   * l'exécuter en effectuant la commande suivante :
   
    spark-submit --class com.upmc.dar.App  --master local  BrooklynHousePricing.jar input nbr
    
avec 

* **input :** le chemin absolue du fichier csv à analyser 
* **nbr :** le pourcentage du contenu à analyser (valeur comprise entre [1,100])

## Exemple pour évaluer 40% du dataset

    spark-submit --class com.upmc.dar.App  --master local  BrooklynHousePricing.jar /BrooklynHousePricing/files/brooklyn_sales_map.csv 40