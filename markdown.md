# Výsledky testovania priestorových stromov

Testovanie prebiehalo v 5 behoch, pričom každý beh zahŕňal 10 000 vložení bodov, 10 000 vyhľadávaní konkrétnych bodov a 100 intervalových vyhľadávaní. Výsledky predstavujú medián nameraných časov.

| Implementácia            | Insert (ms) | Find (ms) | Range search (ms) | Celkom (ms) |
|--------------------------|------------:|----------:|------------------:|------------:|
| My KDTree                |       5,266 |     5,056 |             0,512 |      10,833 |
| My QuadTree              |       5,845 |     6,182 |             0,806 |      12,833 |
| My Quad-KDTree           |       5,069 |     4,694 |             2,913 |      12,676 |
| Existing external KDTree |       6,242 |     6,264 |            21,801 |      34,307 |
| TinSpin KDTree           |       4,254 |    25,984 |             0,446 |      30,683 |
| JTS KDTree               |      11,958 |    13,796 |             0,325 |      26,078 |
| Varun Pant QuadTree      |       4,171 |     4,316 |             0,069 |       8,557 |
| Alwex QuadTree           |      18,314 |   128,441 |             5,067 |     151,822 |
| Ryan Pelletier QuadTree  |       3,069 |  1139,936 |            10,828 |    1153,832 |

Najlepší celkový čas dosiahol **Varun Pant QuadTree (8,557 ms)**, ktorý bol zároveň najrýchlejší pri vyhľadávaní konkrétnych bodov aj intervalov. Najrýchlejšie vkladanie dosiahol Ryan Pelletier QuadTree (3,069 ms), ale jeho vyhľadávanie bodov bolo výrazne pomalšie.

Vlastný K-d strom dosiahol celkový čas 10,833 ms a pri intervalovom vyhľadávaní čas 0,512 ms. Oproti implementácii Existing external KDTree bol pri tejto operácii približne 42,6-krát rýchlejší.

Najvýraznejšie výkonnostné problémy sa prejavili pri vyhľadávaní bodov v implementáciách Alwex QuadTree a Ryan Pelletier QuadTree. Pri Existing external KDTree bolo naopak najpomalšie intervalové vyhľadávanie.

Všetky testované priestorové stromy úspešne prešli kontrolami správnosti intervalového vyhľadávania. Výsledky platia pre použitý dataset a testovacie podmienky.