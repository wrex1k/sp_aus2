# Pomôcka k obhajobe: súbory a metódy

## Tri vety na obhajobu
1. Spoločná abstraktná trieda `SpatialTree` umožňuje porovnať moje aj cudzie stromy rovnakým benchmarkom.
2. Aplikácia používa MVC: model drží dáta a strom, controller riadi tok, view zobrazuje (konzola aj GUI).
3. `findNearby` robí dva kroky: `rangeSearch` v obdĺžniku (hrubý filter) a presný filter podľa vzdialenosti v metroch. Mazanie je „soft“, strom sa nemení.

## Flow aplikácie podľa vstupu používateľa

Základné pravidlo: **View** pýta vstup a zobrazuje, **Controller** vstup posiela, **Model** robí logiku a pracuje so stromom. Chyby (`IOException`, `IllegalArgumentException`, `IllegalStateException`) zachytí `run()` v controlleri a zobrazí „Operation failed: ...“.

### 0. Štart (konzola: `MainApplication.main`)
```
MainApplication.main
├─ ConsoleView.chooseStructure()      -> KD_TREE / QUAD_TREE / QUAD_KD_TREE
├─ ConsoleView.askCsvPath()           -> cesta k CSV
├─ new PoiManagerModel(structure)     -> createTree(structure) vytvorí prázdny strom
├─ new PoiManagerController(model, view, csvPath)
└─ controller.loadAtStartup()
   ├─ model.loadFromCsv(csvPath)
   │  ├─ PoiCsvRepository.read(path)
   │  │  ├─ kontrola súboru (existuje? je súbor? čitateľný?)
   │  │  ├─ kontrola hlavičky (id;name;description;lat;lon;address;created_at;updated_at)
   │  │  └─ každý riadok: 8 stĺpcov? duplicitné ID? -> new Poi(...) (validácia)
   │  └─ replaceContents(pois)        -> nový strom, vloží všetky POI, až potom vymení
   ├─ chyba -> "Could not load CSV: ..." -> program skončí
   └─ OK -> "Loaded POIs: N" -> controller.run()
```

### 0b. Štart (GUI: `PoiManagerFxLauncher` -> `PoiManagerFxApplication.start`)
```
PoiManagerFxApplication.start
├─ chooseSpatialStructure()      (zrušenie -> Platform.exit)
├─ new PoiManagerController(new PoiManagerModel(structure))
├─ cyklus: chooseCsvFile()       (otvoriť existujúci alebo vytvoriť nový)
│  ├─ nový súbor    -> controller.saveToCsv(path)
│  ├─ existujúci    -> controller.loadFromCsv(path)
│  └─ chyba -> showError a zopakuje výber
├─ createLayout()                (mapa PoiMapView + tabuľka PoiTableView + ovládanie)
├─ showAllPois(1)                (zobrazí prvú časť všetkých POI)
└─ stage.show()
```

### Hlavný cyklus (konzola): `controller.run()`
```
run()  (while running)
└─ choice = view.showMenu()
   ├─ 1  -> findAt()
   ├─ 2  -> findNearby()
   ├─ 3  -> create()
   ├─ 4  -> update()
   ├─ 5  -> findByName()
   ├─ 6  -> delete()
   ├─ 7  -> view.showPois(model.getAll())
   ├─ 8  -> generate()
   ├─ 9  -> save()
   ├─ 0  -> save() a koniec
   ├─ 10 -> koniec bez uloženia
   └─ iné -> "Invalid choice."
```

### 1. Nájsť POI na presnej pozícii (menu 1)
```
Používateľ zadá lat, lon
└─ controller.findAt()
   ├─ view.askLatitude(), view.askLongitude()
   └─ model.findAt(lat, lon)
      ├─ Poi.validateCoordinates(lat, lon)
      ├─ tree.find(new Point(lat, lon))     <- presné hľadanie v strome
      ├─ nenájdené -> prázdny zoznam
      └─ activeOnly(...)                    <- odfiltruje soft-delete
   └─ view.showPois(výsledok)
```

### 2. Nájsť POI v okolí (menu 2) – dva kroky
```
Používateľ zadá lat, lon, polomer (m)
└─ controller.findNearby()
   └─ model.findNearby(lat, lon, radius)
      ├─ kontrola: polomer konečný a >= 0
      ├─ angularRadius = radius / EARTH_RADIUS   (uhlová vzdialenosť)
      ├─ minLat/maxLat = lat ± polomer v stupňoch
      ├─ KROK 1: hrubý filter obdĺžnikom -> kandidáti
      │  ├─ blízko pólu alebo veľký polomer -> tree.rangeSearch(celé lon -180..180)
      │  └─ inak: minLon/maxLon = lon ± longitudeRadius
      │     └─ queryLongitudeRange(...)
      │        ├─ minLon < -180 -> 2 rangeSearch, výsledky combine()
      │        ├─ maxLon > 180  -> 2 rangeSearch, výsledky combine()
      │        └─ inak          -> 1 rangeSearch
      └─ KROK 2: presný filter
         └─ pre každého kandidáta: nie je zmazaný A distanceMeters <= polomer
   └─ view.showPois(výsledok)
```
Prečo dva kroky: strom vie hľadať len **obdĺžnik**, ale kruh v metroch je presnejší. Obdĺžnik rýchlo zúži kandidátov, vzdialenosť ich potom presne overí.

### 3. Vytvoriť POI (menu 3)
```
Používateľ zadá name, description, lat, lon, address
└─ controller.create()
   ├─ view.askCreateInput()
   └─ controller.create(input) -> model.create(...)
      ├─ Poi.create(...)         <- UUID, Instant.now() pre created aj updated, validácia
      ├─ tree.insert(poi)        <- false => IllegalStateException (napr. QuadTree max hĺbka)
      └─ pois.add(poi)
   └─ view.showMessage("Created POI with ID: ...")
```

### 4. Upraviť POI (menu 4)
```
Používateľ zadá lat, lon, polomer
└─ controller.update()
   ├─ candidates = model.findNearby(lat, lon, radius)
   ├─ selected = view.selectPoi(candidates)   <- zvolí zo zoznamu, < 0 = zrušiť
   ├─ view.askEditInput(current)              <- Enter nechá pôvodnú hodnotu
   └─ model.update(id, name, description, lat, lon, address)
      ├─ findIndex(id)                        <- aktívny POI, inak výnimka
      ├─ new Poi(rovnaké id a createdAt, nové hodnoty, updatedAt = Instant.now())
      ├─ kópia zoznamu, na index dá nový Poi
      └─ replaceContents(...)                 <- postaví NOVÝ strom zo všetkých POI
   └─ view.showMessage("Updated POI with ID: ...")
```
Record je nemenný, preto sa vytvára nový objekt a strom sa prestavuje (O(n), ale bezpečné).

### 5. Hľadať podľa mena v oblasti (menu 5)
```
Používateľ zadá text a obdĺžnik (minLat, minLon, maxLat, maxLon)
└─ controller.findByName()
   ├─ view.askSearchText(), view.askArea()
   └─ model.findByNameInArea(text, minLat, minLon, maxLat, maxLon)
      ├─ validateRange (min <= max, platné súradnice) a text nesmie byť prázdny
      ├─ tree.rangeSearch(min, max)             <- všetko v obdĺžniku
      ├─ filter: nie je zmazaný
      └─ filter: normalize(name) obsahuje normalize(text)   (bez diakritiky a veľkosti písmen)
   └─ view.showPois(výsledok)
```

### 6. Zmazať POI (menu 6)
```
Používateľ zadá lat, lon, polomer
└─ controller.delete()
   ├─ candidates = model.findNearby(...)
   ├─ selected = view.selectPoi(candidates)
   └─ model.delete(poi)
      ├─ findIndex(id)                <- musí byť aktívny
      └─ deletedIds.add(id)           <- SOFT delete, strom aj zoznam zostávajú
   └─ view.showMessage("Deleted POI with ID: ...")
```
Zmazaný POI zostane v strome. Preto všetky vyhľadávania filtrujú podľa `deletedIds` (`activeOnly`, filter v `findNearby` a `findByNameInArea`). `getAll` ho preto nevráti a `saveToCsv` ho neuloží.

### 7. Zobraziť všetko (menu 7)
```
view.showPois(model.getAll())   <- aktívne POI (bez zmazaných)
```

### 8. Vygenerovať testovacie POI (menu 8)
```
Používateľ zadá počet
└─ controller.generate()
   └─ model.generateRandom(count)
      ├─ count < 0 -> výnimka
      └─ count x: náhodné lat/lon v testovacej oblasti, new Poi(...), tree.insert, pois.add
   └─ view.showMessage("Generated POIs: N")
```

### 9. Uložiť (menu 9 alebo 0)
```
controller.save()
└─ model.saveToCsv(csvPath)
   └─ PoiCsvRepository.write(path, getAll())   <- len aktívne POI, vždy s hlavičkou
└─ view.showMessage("Saved POIs: N")
```
Menu 0 uloží a skončí. Menu 10 skončí bez uloženia.

### 10. Zmena štruktúry za behu (GUI)
```
controller.setSpatialStructure(structure) -> model.setStructure(structure)
├─ rovnaká štruktúra -> nič nerobí
├─ createTree(newStructure)
├─ všetky POI (aj zmazané) sa vložia do nového stromu
│  └─ odmietnutie -> IllegalStateException (QuadTree: max hĺbka 32), pôvodný strom ostane
└─ až po úspechu: tree = nový strom
```

## Čo sa deje v strome (pre každú operáciu)
| Operácia v aplikácii | Metóda stromu | Čo to robí |
|---|---|---|
| vytvoriť POI, načítať CSV, generovať, update | `insert` | nájde miesto podľa súradníc, duplicity idú do `values` |
| hľadať na pozícii | `find` | presný bod, `Optional` zoznamu |
| okolie, hľadanie podľa mena | `rangeSearch` | všetko v obdĺžniku, preskakuje podstromy mimo oblasti |
| zmazať | žiadna | strom sa nemení (soft delete) |

## Súbory a ich zodpovednosť

### `sk/fri/` a `structures/`
| Súbor | Zodpovednosť |
|---|---|
| `MainApplication` | štart konzolovej verzie |
| `SpatialTree` | abstraktný základ: `getName`, `insert`, `find`, `rangeSearch` |
| `KDTree` | vlastný k-d strom (os sa strieda podľa `depth % k`) |
| `QuadTree` | vlastný quad strom (4 deti, `MAX_DEPTH = 32`) |
| `QuadKDTree` | quad strom, ktorého listy sú malé KD stromy (`LEAF_CAPACITY = 32`, `MAX_DEPTH = 32`) |
| `Node` | uzly: `Node`, `KDNode`, `QuadNode`, `QuadKDNode` |
| `Point`, `KDPoint` | 2D bod a bod s k súradnicami |

### `poi/model/`
| Súbor | Zodpovednosť |
|---|---|
| `Poi` | record (nemenný) s validáciou v konštruktore |
| `PoiManagerModel` | logika: zoznam POI, strom, hľadanie, create/update/delete, CSV |
| `PoiCsvRepository` | čítanie a zápis CSV (`;`, 8 stĺpcov, pevná hlavička) |
| `SpatialStructures` | enum `KD_TREE`, `QUAD_TREE`, `QUAD_KD_TREE` |

### `poi/controller/`
| Súbor | Zodpovednosť |
|---|---|
| `PoiManagerController` | spája model a view, menu cyklus `run`, zachytáva chyby |

### `poi/view/`
| Súbor | Zodpovednosť |
|---|---|
| `PoiManagerView` | rozhranie view (+ `CreateInput`, `EditInput`) |
| `ConsoleView` | terminálové menu a vstupy |
| `PoiManagerFxLauncher` | `main` pre GUI |
| `PoiManagerFxApplication` | hlavné okno JavaFX, dialógy, operácie |
| `PoiMapView` | mapa Slovenska, kreslenie bodov, klik |
| `PoiTableView` | tabuľka s checkboxmi |
| `PoiControlsView` | panel tlačidiel |

### `adapters/`
Obaľujú cudzie knižnice do rozhrania `SpatialTree`.

| Súbor | Obaľuje |
|---|---|
| `KDTree2DAdapter` | môj `KDTree` na 2D `Point` |
| `kdAdapters/JtsKDAdapter` | JTS KD strom |
| `kdAdapters/TinSpinKDAdapter` | TinSpin KD strom |
| `kdAdapters/KDAdapter` | externý KD strom (`external/.../KDtree`) |
| `quadAdapters/AlwexQuadTreeAdapter` | Alwex quad strom |
| `quadAdapters/RyanQuadTreeAdapter` | java-simple-quadtree (Ryan) |
| `quadAdapters/VarunPantQuadTreeAdapter` | Varun Pant quad strom |

### `tester/`
| Súbor | Zodpovednosť |
|---|---|
| `LoadCSV` | `loadTestPoints` (slovakia_nodes.csv), `loadTestAreas` (testovacie_oblasti.csv) |
| `CsvPoint`, `Area` | dáta bodu a oblasti |
| `TreeTester` | `get2DTrees` (zoznam stromov), `testTrees` |
| `TreeBenchmark` | meria insert, find, range search; 2 zahrievacie + 5 meraných behov, medián |

### Zdroje (`src/main/resources/`)
- `csv/slovakia_nodes.csv` (`id;lat;lon`) a `csv/testovacie_oblasti.csv` používa benchmark.
- `map/slovakia-border.txt` je hranica pre `PoiMapView`.
- Formát CSV pre hlavnú aplikáciu je iný: `id;name;description;lat;lon;address;created_at;updated_at`.

## Metódy: stromy

### `SpatialTree<T,P>`
- `insert(value)`: vloží hodnotu, vráti `true` alebo `false`.
- `find(point)`: hodnoty na presnom bode (`Optional`).
- `rangeSearch(min, max)`: hodnoty v obdĺžniku.

### `QuadTree`
- `insert`: prázdny -> koreň. Rovnaké súradnice -> pridá do `values`. Hĺbka >= 32 -> `false`. Inak vyberie kvadrant, a ak je prázdny, vytvorí uzol, ak nie, ide hlbšie.
- `find`: ide po kvadrantoch, kým nenájde rovnaký bod, inak `empty`.
- `rangeSearch`: zásobník, pridá `values` uzlov v obdĺžniku, potomkov len ak `intersectsQuadrant`.
- `intersectsQuadrant`: zasiahne obdĺžnik daný kvadrant? Pravá strana: `max >= stred`. Ľavá strana: `min < stred`. Platí v x aj y (AND).
- `quadrant`: index 0-3. `x >= cx` dá +1, `y >= cy` dá +2.
  - 0 vľavo dole, 1 vpravo dole, 2 vľavo hore, 3 vpravo hore.
- `contains`: je bod v obdĺžniku (hranice patria dnu).
- `isSame`: rovnaké x aj y.
- `validateRange`: `min` nesmie byť väčšie ako `max`.

### `KDTree`
- `insert`: rovnaký bod -> `values`. Inak na osi `depth % k`: `current > point` doľava, inak doprava.
- `find`: rovnaký princíp ako `insert`.
- `rangeSearch`: zásobník s hĺbkou. Ľavý podstrom ak `min[os] <= split`, pravý ak `max[os] >= split`.
- `compare(a, b, depth)`: porovná súradnice na osi danej hĺbkou.
- `validatePoint`: správny počet dimenzií. `validateRange`, `contains`, `isSame` ako v QuadTree.

### `QuadKDTree`
- `insert`: v liste vloží do KD stromu, ak bod existuje, je miesto (`distinctPoints < 32`) alebo je max. hĺbka. Inak `split`.
- `split`: presunie všetky hodnoty podľa stredu do 4 detí. Starý list sa zmení na vnútorný uzol (`data = null`, `values = null`).
- `find`: zostúpi do listu a hľadá v jeho KD strome.
- `rangeSearch`: v listoch volá KD `rangeSearch`, vo vnútorných uzloch používa `intersectsQuadrant`.
- `newNode`: nový list s novým KD stromom.
- `validatePoint`: oba extraktory musia dať rovnaké súradnice.
- `toKDPoint`: `Point` -> `KDPoint`.

## Metódy: model a controller

### `PoiManagerModel`
- `createTree`: podľa enumu vytvorí `KDTree2DAdapter`, `QuadTree` alebo `QuadKDTree`.
- `setStructure`: postaví novú štruktúru zo všetkých POI.
- `getAll` (aktívne), `getAllIncludingDeleted`, `isDeleted`.
- `findAt`: presná pozícia cez `tree.find`.
- `findNearby`: `rangeSearch` v štvorci (kandidáti) + presný filter vzdialenosti.
- `queryLongitudeRange`: rozdelí dotaz pri dátumovej hranici (±180).
- `findByNameInArea`: `rangeSearch` + filter podľa mena (bez diakritiky a veľkosti písmen).
- `create`: nový POI a vloženie. `update`: nový `Poi` + **prestavba celého stromu**.
- `delete`: **soft delete** (`deletedIds`), strom sa nemení.
- `generateRandom`, `loadFromCsv`, `saveToCsv`.
- `replaceContents`: postaví strom bokom, vymení ho až keď všetko prejde (všetko alebo nič).
- `findIndex`, `activeOnly`, `combine`, `normalize`, `distanceMeters`, `validateRange`.

### `Poi` (record)
- Kompaktný konštruktor `public Poi { ... }` sa volá pri každom `new Poi(...)`: validuje, `name.trim()`, `null` -> prázdny text, kontroluje súradnice a časy.
- `create`: UUID + jedno `Instant.now()` pre `createdAt` aj `updatedAt`.
- `validateCoordinates`: `Double.isFinite` (nie NaN ani nekonečno), lat -90..90, lon -180..180.

### `PoiCsvRepository`
- `read`: kontrola súboru, hlavičky, 8 stĺpcov, duplicitných ID, formátu čísel a časov.
- `write`: zapíše hlavičku a riadky. Pomocné: `parseDouble`, `parseInstant`, `invalidValue`, `quote`.

### `PoiManagerController`
- `loadAtStartup`: načíta CSV pred menu.
- `run`: cyklus menu, `switch` 1-10, chyby ako „Operation failed“.
- Súkromné `findAt`, `findNearby`, `create`, `update`, `findByName`, `delete`, `generate`, `save`: zbierajú vstup z view a volajú model.
- Verejné metódy (`create`, `update`, `findNearby`, `getAllPois`...) delegujú na model, používa ich GUI.

## Metódy: view
- `ConsoleView`: `chooseStructure`, `askCsvPath`, `showMenu`, `ask*`, `selectPoi`, `showPois`, `showMessage`, `read*`.
- `PoiManagerFxApplication`: `start`, `chooseSpatialStructure`, `chooseCsvFile`, `createLayout`, `createMenuBar`, otvorenie a vytvorenie súboru, operácie (`findAt`, `findNearby`, `createPoi`, `editPoi`, `searchByName`, `deletePoi`, `generatePois`, `savePois`, `showAllPois`), `refreshResults`, `applyFilter`, `updateMap`, pomocné metódy pre dialógy a validáciu.
- `PoiMapView`: `showPois`, `render`, `handleClick`, `unproject`/`x`/`y` (prepočet GPS <-> pixely), `loadBorder`.
- `PoiTableView`: `getCheckedPois`, `retainChecks`.

## Benchmark
- `TreeBenchmark.compareStructures`: pre každý strom 2 zahrievacie a 5 meraných behov, `System.nanoTime()`, vypíše medián insert/find/range search.
- Ak strom odmietne niektoré vloženia (QuadTree pri hĺbke 32), počet nasledujúcich `find` sa medzi stromami líši.

## Čo sa môže meniť na cvičení
| Zmena | Kde |
|---|---|
| Iný formát CSV | `HEADER`, `columns.length`, `new Poi(...)` v `PoiCsvRepository` |
| Nové pole v `Poi` | `Poi`, `PoiCsvRepository`, `create`/`update` v modeli, `CreateInput`/`EditInput`, view |
| Nová operácia | metóda v modeli, `case` v `run()`, položka menu vo view |
| Skutočné mazanie | `delete` v modeli (prebudovať strom) |
