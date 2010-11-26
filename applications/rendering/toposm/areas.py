#!/usr/bin/python
from mapnik import Envelope

# Misc test areas
WA = Envelope(-125.3, 45.4, -116.8, 49.1)
WAdetail = Envelope(-123.4, 46.2, -120.0, 48.1)
Seattle = Envelope(-122.4, 47.5, -122.2, 47.7)
WAnw = Envelope(-125.3, 47.7, -121, 49.1)
WAne = Envelope(-121, 47.7, -116.8, 49.1)
WAsw = Envelope(-125.3, 45.4, -121, 47.7)
WAse = Envelope(-121, 45.4, -116.8, 47.7)
NEdetail = Envelope(-71.5, 42, -70.5, 43)
Stow = Envelope(-71.55, 42.40, -71.46, 42.47)
BostonSS = Envelope(-71.2, 42.0, -70.6, 42.4)
BostonDetail = Envelope(-71.11, 42.30, -70.99, 42.41)
COdetail = Envelope(-105.1, 38.7, -104.7, 39.0)
COminor = Envelope(-105.0, 38.8, -104.8, 38.95)

NEdetail1 = Envelope(-71.0, 42.0, -70.5, 42.5)
NEdetail2 = Envelope(-71.0, 42.5, -70.5, 43.0)
NEdetail3 = Envelope(-71.5, 42.0, -71.0, 42.5)
NEdetail4 = Envelope(-71.5, 42.5, -71.0, 43.0)

# Main US zones
US = Envelope(-126, 24, -66, 56)
USnw = Envelope(-126, 40, -96, 56)
USne = Envelope(-96, 40, -66, 56)
USsw = Envelope(-126, 24, -96, 40)
USse = Envelope(-96, 24, -66, 40)

# US UTM Zones
UTM10S = Envelope(-126, 32, -120, 40)
UTM10T = Envelope(-126, 40, -120, 48)
UTM10U = Envelope(-126, 48, -120, 56)
UTM11R = Envelope(-120, 24, -114, 32)
UTM11S = Envelope(-120, 32, -114, 40)
UTM11T = Envelope(-120, 40, -114, 48)
UTM11U = Envelope(-120, 48, -114, 56)
UTM12R = Envelope(-114, 24, -108, 32)
UTM12S = Envelope(-114, 32, -108, 40)
UTM12T = Envelope(-114, 40, -108, 48)
UTM12U = Envelope(-114, 48, -108, 56)
UTM13R = Envelope(-108, 24, -102, 32)
UTM13S = Envelope(-108, 32, -102, 40)
UTM13T = Envelope(-108, 40, -102, 48)
UTM13U = Envelope(-108, 48, -102, 56)
UTM14R = Envelope(-102, 24, -96, 32)
UTM14S = Envelope(-102, 32, -96, 40)
UTM14T = Envelope(-102, 40, -96, 48)
UTM14U = Envelope(-102, 48, -96, 56)
UTM15R = Envelope(-96, 24, -90, 32)
UTM15S = Envelope(-96, 32, -90, 40)
UTM15T = Envelope(-96, 40, -90, 48)
UTM15U = Envelope(-96, 48, -90, 56)
UTM16R = Envelope(-90, 24, -84, 32)
UTM16S = Envelope(-90, 32, -84, 40)
UTM16T = Envelope(-90, 40, -84, 48)
UTM16U = Envelope(-90, 48, -84, 56)
UTM17R = Envelope(-84, 24, -78, 32)
UTM17S = Envelope(-84, 32, -78, 40)
UTM17T = Envelope(-84, 40, -78, 48)
UTM17U = Envelope(-84, 48, -78, 56)
UTM18R = Envelope(-78, 24, -72, 32)
UTM18S = Envelope(-78, 32, -72, 40)
UTM18T = Envelope(-78, 40, -72, 48)
UTM18U = Envelope(-78, 48, -72, 56)
UTM19R = Envelope(-72, 24, -66, 32)
UTM19S = Envelope(-72, 32, -66, 40)
UTM19T = Envelope(-72, 40, -66, 48)
UTM19U = Envelope(-72, 48, -66, 56)

# Cities / Metro areas
Boston = Envelope(-71.36, 42.13, -70.70, 42.60)
NewYorkCity = Envelope(-74.39, 40.50, -73.56, 41.11)
Philadelphia = Envelope(-75.43, 39.81, -74.88, 40.19)
WashingtonDC = Envelope(-77.33, 38.66, -76.79, 39.10)
Detroit = Envelope(-83.58, 42.04, -82.82, 42.71)
Chicago = Envelope(-88.54, 41.45, -87.29, 42.42)
Indianapolis = Envelope(-86.38, 39.61, -85.95, 39.97)
MinneapolisStPaul = Envelope(-93.72, 44.62, -92.65, 45.33)
DenverBoulderCoSprings = Envelope(-105.38, 38.66, -104.52, 40.13)
SaltLakeCityOgdenProvo = Envelope(-112.3, 40.05, -111.34, 41.4)
SeattlePugetOlympics = Envelope(-124.84, 46.91, -121.32, 48.56)
Portland = Envelope(-123.19, 45.17, -122.18, 45.89)
SanFranciscoBay = Envelope(-123.04, 36.93, -121.58, 38.19)
LosAngeles = Envelope(-120.07, 33.40, -116.88, 34.94)
SanDiego = Envelope(-117.47, 32.43, -116.83, 33.24)
Phoenix = Envelope(-112.75, 33.14, -111.43, 33.83)
LasVegas = Envelope(-115.39, 35.92, -114.69, 36.39)
SantaFe = Envelope(-106.15, 35.49, -105.76, 35.83)
Albuquerque = Envelope(-106.87, 34.89, -106.25, 35.34)
Houston = Envelope(-95.62, 29.53, -94.86, 29.96)
DallasFortWorth = Envelope(-97.60, 32.53, -96.47, 33.06)
SanAntonio = Envelope(-98.70, 29.27, -98.32, 29.59)
NewOrleans = Envelope(-90.64, 29.44, -89.59, 30.46)
Atlanta = Envelope(-84.64, 33.49, -84.11, 34.02)
Jacksonville = Envelope(-81.80, 30.11, -81.32, 30.48)
OrlandoTitusville = Envelope(-81.75, 27.96, -80.38, 28.91)
FloridaSE = Envelope(-80.65, 24.90, -79.99, 27.96)

Cities = [Boston, NewYorkCity, Philadelphia, WashingtonDC, Detroit,
    Chicago, Indianapolis, MinneapolisStPaul, DenverBoulderCoSprings,
    SaltLakeCityOgdenProvo, SeattlePugetOlympics, Portland,
    SanFranciscoBay, LosAngeles, SanDiego, Phoenix, LasVegas, SantaFe,
    Albuquerque, Houston, DallasFortWorth, SanAntonio, NewOrleans,
    Atlanta, Jacksonville, OrlandoTitusville, FloridaSE]


# Nature Areas
YellowstoneTetons = Envelope(-111.26, 43.50, -109.76, 45.13)
OregonCascades = Envelope(-123.26, 41.94, -119.88, 42.20)
SierraNevN = Envelope(-122.67, 38.72, -119.64, 42.07)
SierraNevC = Envelope(-121.35, 36.74, -116.88, 38.77)
SierraNevS = Envelope(-119.42, 35.29, -116.12, 36.79)
GrandCanyon = Envelope(-114.92, 35.70, -111.56, 36.93)
Zion = Envelope(-113.25, 37.12, -112.83, 37.52)
Bryce = Envelope(-112.31, 37.40, -112.03, 37.74)
ArchesCanyonlands = Envelope(-110.27, 37.92, -109.26, 38.88)
CapitolReef = Envelope(-111.45, 37.56, -110.82, 38.56)
MesaVerde = Envelope(-108.59, 37.12, -108.31, 37.37)
Glacier = Envelope(-114.51, 48.22, -113.21, 49.02)
RockyMountains = Envelope(-108.81, 35.84, -104.44, 40.94)
Acadia = Envelope(-68.48, 44.10, -68.11, 44.48)
GreatSmokyMountains = Envelope(-84.03, 35.41, -82.99, 35.80)
GuadulupeCarlsbad = Envelope(-105.2, 31.72, -104.21, 32.55)
MammothCave = Envelope(-86.40, 37.04, -85.87, 37.35)
NorthCascades = Envelope(-122.00, 48.18, -120.60, 49.01)
Badlands = Envelope(-102.98, 43.43, -101.79, 44.00)
BlackHills = Envelope(-104.78, 43.49, -103.21, 44.69)
GreatSandDunes = Envelope(-105.74, 37.64, -105.39, 37.95)
WhiteSands = Envelope(-106.84, 32.28, -105.34, 33.51)
GreenMountains = Envelope(-73.23, 43.81, -72.61, 44.76)
WhiteMountains = Envelope(-72.07, 43.73, -70.69, 44.78)
Adirondacks = Envelope(-75.40, 43.11, -73.35, 44.98)
EvergladesKeys = Envelope(-81.84, 24.50, -80.22, 25.91)
NiagaraFalls = Envelope(-79.43, 42.77, -78.66, 43.32)

Nature = [YellowstoneTetons, OregonCascades, SierraNevN,
    SierraNevC, SierraNevS, GrandCanyon, Zion, Bryce,
    ArchesCanyonlands, CapitolReef, MesaVerde, Glacier,
    RockyMountains, Acadia, GreatSmokyMountains,
    GuadulupeCarlsbad, MammothCave, NorthCascades, Badlands,
    BlackHills, GreatSandDunes, WhiteSands, GreenMountains,
    WhiteMountains, Adirondacks, EvergladesKeys, NiagaraFalls]

