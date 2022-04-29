# coding=UTF-8
import pandas as pd
import os
import csv
import re

filePath =  "./"
miaFile = "mia_osm21_pois.csv"
pizFile = "piz_osm21_pois.csv"
str= "addr:street -> MacArthur Causeway)"
pattern = "(?<=-> ).*"
patternHasbrackets = "(?<=-> ).*?(?=\))"


dic = dict()
names = []
n=0
# test =" addr:street -> 16th Street"
# print(test.startswith(" addr"))
with open(filePath+miaFile,'rt') as myFile:
    lines=csv.reader(myFile)
    for line in lines:
        if len(line)==5 and line[1].startswith(" addr"): 
            #\t is Tab 
            n+=1
            lat = line[0].split("\t")[1]
            lon = line[0].split("\t")[0]
            street = re.search(pattern,line[1]).group()
            if names.count(street)==0:
                names.append(street)
            postcode = line[2]
            housenumber = line[3]
            city = line[4]
            dic.setdefault(street+"-lat",[]).append(lat)
            dic.setdefault(street+"-lon",[]).append(lon)
            dic.setdefault(street+"-street",[]).append(street)
            dic.setdefault(street+"-postcode",[]).append(re.search(pattern,postcode).group())
            dic.setdefault(street+"-housenumber",[]).append(re.search(pattern,housenumber).group())
            dic.setdefault(street+"-city",[]).append(re.search(patternHasbrackets,city).group())

i=0
for name in names:
    i+=len(dic.get(name+"-lat"))
    lats = dic.get(name+"-lat")
    lons = dic.get(name+"-lon")
    streets = dic.get(name+"-street")
    postcodes = dic.get(name+"-postcode")
    housenumbers = dic.get(name+"-housenumber")
    citys = dic.get(name+"-city")
    dfmap = {"lat":lats,"lon":lons,"street":streets,"postcode":postcodes,"housenumber":housenumbers,"city":citys}
    df = pd.DataFrame(dfmap)
    #print(df)
    df.to_csv(filePath+"osm21_pois_"+name+".csv",index=False)
    print("ok")
print("Finally ok")
print(n)#5521
print(i)#5521 equal
        

# filePath =  "G:\\IdeaProject\\spadas\\dataset\\poi\\"
# f="mia_osm21_pois.csv"
# df = pd.read_csv(filePath+f)
# print(df)
# lats=[]
# lons=[]
# street=[]
# postcode = []
# housenumber=[]
# city = []

# for row in df.itertuples():
#     if df['CITY_NAME'][1]=='MIA':
#         CITY = Mia
#         x = mia_utm_x+getattr(row, 'X')
#         y = mia_utm_y+getattr(row, 'Y')
#     else:
#         CITY = Pit            
#         x = piz_utm_x+getattr(row, 'X')
#         y = piz_utm_y+getattr(row, 'Y')
#     pair = utm.to_latlon(x, y, utm_zone, utm_band)
#     lats.append(pair[0])
#     lons.append(pair[1])
#     #loc = "%s" % (CITY)
#     #df['Location'] = loc 
# df['latitude'] = lats
# df['longitude'] = lons
# df.drop(columns=["Location"])
# # index参数解决多出来一列
# df.to_csv(filePath+f,index=False)