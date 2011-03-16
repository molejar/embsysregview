# needs lynx, wget, java
# a compiled Convert.class file in the h2xml folder
# and the lm3*.h files in a folder called "inc"
rm -Rf txt
rm -Rf xml
mkdir txt
mkdir xml

for i in $(ls inc/lm3s*.h|cut -d'/' -f 2|cut -d'.' -f 1)
do 
	echo downloading ${i}.html ...
	wget -c http://www.luminarymicro.com/products/${i}.html -O - 2>/dev/null|grep -A 1000 "Product Features" |grep -B 1000 "article_seperator"|lynx -dump -stdin > txt/${i}.txt
	echo $'\n'$'\n'"Copyright � 2006-2011 Texas Instruments Incorporated. All rights reserved." >> txt/${i}.txt
	echo generating ${i}.xml ...
	java -cp h2xml/jdom.jar:. h2xml.Convert inc/${i}.h txt/${i}.txt|sed -e"s/\"INC\//\"/g" >xml/${i}.xml
done
