import pandas as pd
import os
import utm

### config 
utm_zone = 17
utm_band = 'T'
Mia_pre = 'MIA'
Pit_pre = 'PIT'
Mia = 'Miami'
Pit = 'Pittsburgh'
piz_utm_x = 583710.0070
piz_utm_y = 4477259.9999
mia_utm_x = 580560.0088
mia_utm_y = 2850959.9999


# df = pd.read_csv('1.csv')
# if df['CITY_NAME'][1]=='MIA':
#     CITY = Mia
# else:
#     CITY = Pit
# mean_x = piz_utm_x+df['X'].mean()
# mean_y = piz_utm_y+df['Y'].mean()
# pair = utm.to_latlon(mean_x, mean_y, utm_zone, utm_band)
# loc = "%s,UT(%f,%f)" % (CITY,pair[0],pair[1])
# df['Location'] = loc 
# df.to_csv('1.csv')

filePath =  "G:\\IdeaProject\\spadas\\dataset\\argoverse\\"
errfilename = "convertErr_id.txt"
#relativePath = 'train/data/'
for root, dirs, files  in os.walk(filePath):
    for f in files:
        try:
            lats=[]
            lons=[]
            df = pd.read_csv(filePath+f) 
            for row in df.itertuples():
                if df['CITY_NAME'][1]=='MIA':
                    CITY = Mia
                    x = mia_utm_x+getattr(row, 'X')
                    y = mia_utm_y+getattr(row, 'Y')
                else:
                    CITY = Pit            
                    x = piz_utm_x+getattr(row, 'X')
                    y = piz_utm_y+getattr(row, 'Y')
                pair = utm.to_latlon(x, y, utm_zone, utm_band)
                lats.append(pair[0])
                lons.append(pair[1])
                #loc = "%s" % (CITY)
                #df['Location'] = loc 
            df['latitude'] = lats
            df['longitude'] = lons
            df.drop(columns=["Location"])
            # index参数解决多出来一列
            df.to_csv(filePath+f,index=False)
            print("ok")
        except Exception as inst :
            # with open(errfilename,'a') as obj:
            #     obj.write(f+"\n")
            print(inst)
            continue
