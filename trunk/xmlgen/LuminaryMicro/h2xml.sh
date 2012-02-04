# needs lynx, wget, java
# a compiled Convert.class file in the h2xml folder
# and the lm*.h files in a folder called "inc"
rm -Rf txt
rm -Rf xml
mkdir txt
mkdir xml

for i in $(ls inc/lm*.h|cut -d'/' -f 2|cut -d'.' -f 1)
do 
	echo downloading ${i}.html ...
	wget -c "http://www.ti.com/components/docs/getproductparametrics.tsp?compVariationId=8586&genericPartNumber=${i}" -O - 2>/dev/null |lynx -dump -stdin -nolist|sed -e"s/Samples$//g;s/Inventory$//g"|grep -v "^[ \t]*$"|grep -vi price|grep "   [^ ].*"|sed -e"s/^[ \t]*//g;s/[ \t]*$//g"|tail -n +2 > txt/${i}.txt
	echo $'\n'$'\n'"Copyright Texas Instruments Incorporated. All rights reserved." >> txt/${i}.txt
	echo generating ${i}.xml ...
	java -cp h2xml/jdom.jar:. h2xml.Convert inc/${i}.h txt/${i}.txt|sed -e"s/\"INC\//\"/g" >xml/${i}.xml
done
