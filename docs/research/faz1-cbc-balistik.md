# Faz 1 — CBC balistik araştırması

Kaynak: Create Big Cannons `v5.11.7+mc.1.21.1` etiketi
(<https://github.com/Cannoneers-of-Create/CreateBigCannons/tree/v5.11.7%2Bmc.1.21.1>).
Aşağıdaki yollar `src/main/java/rbasamoyai/createbigcannons/` altına göredir.

## 1. Mermi fiziği nerede?

`munitions/AbstractCannonProjectile.java` (vanilla `Projectile`'dan türüyor). Hareket tamamen
CBC'nin kendi `tick()`'inde; vanilla `Projectile.tick()` mermiyi hareket ettirmiyor.
Big cannon mermileri (`munitions/big_cannon/AbstractBigCannonProjectile`) `getForces`'i
override etmiyor, yani aşağıdaki model hepsi için geçerli.

Uçuştayken her tick (`tick()`, ~satır 142–153):

```
a      = getForces(pos, v)              // ivme, blok/tick²
pos'   = pos + v + a/2
v'     = v + a
```

`getForces` (satır 188):

```
a = -v̂ · D  +  (0, g, 0)
```

- **Yerçekimi** `g = ballistic.gravity × dimension.gravity_multiplier` (`getDefaultGravity`, satır 608).
  Değer **negatif** saklanıyor (ör. −0.05), işaret çevrilmeden ekleniyor.
- **Sürükleme** `D = min(drag × yoğunluk × |v| [× |v| eğer quadratic_drag], |v|)` (`getDragForce`, satır 613).
  `yoğunluk = dimension.drag_multiplier (+ mermi sıvı içindeyse FluidDragHandler değeri)`.

**Çözücü için önemli sonuç:** `quadratic_drag=false` iken (tüm big cannon mermileri)
`-v̂·k|v| = -k·v` olur, yani ivme hız vektörüne göre **doğrusal**:

```
v(n+1) = (1 − k)·v(n) + G          k = drag × drag_multiplier,  G = (0, g, 0)
pos(n+1) = pos(n) + v(n) + (−k·v(n) + G)/2
```

Bu, kapalı formda çözülebilen doğrusal bir rekürans. Faz 2'de iterasyon yerine formülle hızlı
hesap mümkün (havada, sıvıya girmeden ve `min(..., |v|)` kırpması devreye girmeden; `k < 1` olduğu
sürece kırpma tetiklenmez).

Notlar:
- Çarpışma kontrolü (`clipAndDamage`, satır 218) bir tick ilerisini `v + a` ile tarıyor, gerçek
  hareket `v + a/2`. İsabet noktasında yarım-ivme kadar küçük bir fark olabilir.
- Mermi chunk yüklü değilse tick atlanıyor (satır 103–104). Sunucu config'i
  `projectilesCanChunkload` açıksa RPL üzerinden chunk zorla yükleniyor. RPL'nin fizikte
  gördüğüm başka rolü yok.

## 2. Değerler config'den mi datapack'ten mi?

**Datapack'ten.** Hepsi `SimpleJsonResourceReloadListener`, `/reload` ile değişir ve
istemciye paketle senkronlanır.

| Veri | Datapack yolu | Kod |
|---|---|---|
| Mermi balistiği (`gravity`, `drag`, `quadratic_drag`, …) | `data/<ns>/munition_properties/projectiles/<entity_id>.json` | `munitions/config/MunitionPropertiesHandler`, `components/BallisticPropertiesComponent` |
| Boyut çarpanları | `data/<ns>/dimension_munition_properties/<dimension>.json` | `munitions/config/DimensionMunitionPropertiesHandler` |
| İtici barut gücü/sapma | `data/<ns>/munition_properties/block_propellant/*.json` | `PowderChargeBlock`, `BigCartridgeBlock` |
| Namlu malzemesi (spread, tıkanma) | `data/<ns>/big_cannon_materials/*.json` (varsayılanlar kodda: `index/CBCBigCannonMaterials`) | `cannons/big_cannons/material/BigCannonMaterialProperties` |

Varsayılan değerler (5.11.7):

| Mermi | gravity | drag | quadratic |
|---|---|---|---|
| shot, ap_shot, he_shell, ap_shell, shrapnel_shell, smoke_shell, fluid_shell, drop_mortar_shell, traffic_cone | −0.05 | 0.01 | hayır |
| mortar_stone, autocannon mermileri, machine_gun_bullet | −0.025 | 0.01 | hayır |

| Boyut | gravity_multiplier | drag_multiplier |
|---|---|---|
| overworld | 1 | 1 |
| the_nether | 1.1 | 1.1 |
| the_end | 0.8 | 0 |

JSON'da alan yoksa: `gravity` −0.05, `drag` 0. Yüklemede `gravity ≤ 0`, `drag ≥ 0` olarak kırpılıyor.

Config'den (`CBCConfigs.server()`) gelenler fiziği değil, yan davranışları etkiliyor:
`disableAllFailure`, `dampPropellantWeakensPropellant`, `projectilesCanBounce`, `bigCannonRecoilScale`.

**Sonuç:** Çözücü sabit kodlanmış değil, oyundaki datapack değerlerini okumalı
(`MunitionPropertiesHandler` üzerinden mermi tipine göre + `DimensionMunitionPropertiesHandler`).

## 3. Namlu hızı ve atış yönü

`cannon_control/contraption/MountedBigCannonContraption.fireShot` (satır 266–475):

- **Hız büyüklüğü = `chargesUsed`** (blok/tick). Her powder charge `strength` = **2** ekler
  (`powder_charge.json`); big cartridge `güç seviyesi × 2`. Islak barut `× 0.5`
  (config açıksa). Barut arasındaki her hava boşluğu **−1**. Merminin `added_charge_power`'ı eklenir.
  **Namlu uzunluğu hızı artırmıyor**, sadece sapmayı azaltıyor ve tıkanma (squib) riskini etkiliyor.
- **Sapma (spread):** her barut `added_spread` (2) ekler; barutla mermi arasındaki her namlu
  bloğu malzemenin `spread_reduction_per_barrel` kadar azaltır, alt sınır `minimum_spread`
  (çelik 0.025 / 1.4, bronz 0.03 / 1.4, dökme demir 0.05 / 2, nethersteel 0.02 / 1.15).
- **Atış:** vanilla `Projectile.shoot(dir, chargesUsed, spread)`:
  `v0 = (normalize(dir) + üçgen_gürültü(0.0172275 × spread)) × chargesUsed`
  (her eksen için ayrı, `random.triangle` yani ±`0.0172275 × spread` aralığında üçgen dağılım;
  NeoForge 21.1.265 kaynağında `Projectile.getMovementToShoot` ile doğrulandı). Gürültü
  normalize'dan *sonra* eklendiği için hız büyüklüğü de hafifçe değişiyor.
- **Çıkış noktası:** son namlu bloğunun bir ötesinin merkezi, eksi `2 × dir`.
- **Yön:** `dir = normalize(spawnPos − contraption merkezi)`, yani doğrudan topun dönüşünden
  (yaw/pitch) türetiliyor.
- Şüpheli satır (472 civarı): `if (chargesUsed < minimumSpread) chargesUsed = minimumSpread;`
  sapma alt sınırıyla hızı karşılaştırıyor. Pratikte etkisiz (hız hep > 0.05) ama not edildi.

## 4. Mount açısı nereden okunuyor?

`cannon_control/cannon_mount/CannonMountBlockEntity`:

- `cannonYaw`, `cannonPitch` (derece, `float`). Her tick dönüş hızı (RPM'den
  `getAngularSpeed`) eklenir, yaw `% 360`, pitch `[-maxDepression, maxElevation]` aralığına kırpılır
  (satır 163–166).
- `applyRotation` (satır 183) değerleri `PitchOrientedContraptionEntity`'ye (`mountedContraption.pitch/yaw`)
  yazar. **İşaret tuzağı:** topun ilk yönüne göre `sgn = ±1` çarpanı var (satır 144–146, 189–191),
  ama bu çarpan yalnızca *giriş dönüş yönünü* etkiliyor (`cannonPitch += pitchSpeed × sgn`). `cannonPitch` kendisi
  yönden bağımsız gerçek yükseliş açısı (oyunda doğrulandı), dolayısıyla doğrudan kullanılabilir.
- Sequenced Gearshift ile tam açı: `yaw/pitchInterface.getSequencedAngleLimit()` (satır 151–161).
  **Mount'ta dahili 8:1 redüksiyon var:** hem dönüş hızı (`getAngularSpeed`, satır 317:
  `convertToAngular(rpm) × 0.125`) hem sıralı açı limiti (`CannonMountInterfaceBlockEntity`
  satır 48: `getEffectiveValue(...) × 0.125`) 1/8 ile çarpılıyor. Gearshift'e 160° girilirse
  top 20° döner. Create'in `getEffectiveValue`'su aradaki dişli oranını hesaba kattığı için
  gearshift ile mount arasına 1:8 hızlandırma koymak da aynı sonucu verir.
- Açıyı dışarı veren hazır yer: `getDisplayPitch()` ve `CannonMountDisplaySource` (Display Link).
- Taşınabilir top: `cannon_control/carriage/CannonCarriageEntity`, sabit mount:
  `cannon_control/fixed_cannon_mount`. Faz 3'te bunlar da hesaba katılmalı.

**Calculation table için:** açıyı `CannonMountBlockEntity`'den okumak mümkün ama güvenilir
yükseliş açısı için en temiz yol contraption entity'nin gerçek namlu yönünü
(`toGlobalVector` ile) hesaplamak. Böylece `sgn` belirsizliği ortadan kalkar.

## Oyun içi ölçüm (2026-11-24)

Kurulum: superflat (zemin üstü y = −60), çelik top: 3 cannon chamber + 4 barrel + quick-firing breech,
2 powder charge + solid shot (namlu hızı 4 blok/tick), hep güneye (+z). Mount (−2, −60, −9).
Top mount'un 2 üstünde kuruluyor (`assemblyPos = mount.relative(vertical, −2)`), dönüş merkezi blok
(−2, −58, −9), namlu ucu (−2, −58, −3). Çıkış noktası = merkez + 5 × yön.

Model: yukarıdaki tick denklemleri, gravity −0.05, drag 0.01, düşüş = y'nin −60'ı geçtiği an.

| Açı | Ölçülen z | Model z (blok) | Fark | Uçuş (tick) |
|---|---|---|---|---|
| 0° | 36 | 35 | +1 | 11 |
| 15° | 131 | 130 | +1 | 43 |
| 30° | 177 | 177 | 0 | 75 |
| 45° | 173 | 173 | 0 | 100 |
| 60° | 133 | 132 | +1 | 119 |

Model tüm atışlarda ±1 blok içinde tutuyor; x her atışta −2 (bu kurulumda saçılma ihmal edilebilir).
+1 farkı muhtemelen merminin zemine çarptıktan sonra gömülmesi/çarpışma taramasının `v + a` ile
yapılması. En uzun menzil ~30–35° civarında (sürükleme 45°'yi kısaltıyor).

### Çözücü doğrulaması (FireSolver, ters yön)

Açılar `FireSolver` ile hesaplandı, Sequenced Gearshift'e `açı × 8` (en yakın tam sayı) girildi.
3 barutlu atışlar için config'de top arızası kapatıldı (çelik topun güvenli sınırı 2 barut).

| Test | Hız | Hedef | Açı | Beklenen z | Ölçülen z | Not |
|---|---|---|---|---|---|---|
| A1 | 4 | zemin z=150 | 19.625° | 150 | 150 | z=150'deki oyuncuya doğrudan isabet, sonra sekip 152'ye |
| A2 | 4 | zemin z=150 | 54.625° | 150 | 151 | 150 de kırıldı |
| B1 | 6 | zemin z=250 | 14.5° | 250 | 253 | |
| B2 | 6 | zemin z=250 | 57.75° | 250 | 252 | |
| C1 | 4 | platform z=120 | 18.125° | 120 | 118 | platform yüzeyi y=−49 ise model 117 der (y kontrol edilecek) |

Yorum: A1'deki varlık isabeti, ilk temasın tam modelin öngördüğü noktada olduğunu gösteriyor.
Zemindeki fazlalık (+1…+3) uçuş hatası değil, çarpma sonrası: solid shot yumuşak bloklara (çim,
toprak) girip ilerliyor ya da sekiyor (`clipAndDamage` → `nextVelocity`). Hız arttıkça ve
açı yataylaştıkça fark büyüyor (B1 > B2 > A). Atış çözümü için ilk temas noktası esas alınmalı.

## Açık sorular (oyun içi doğrulama)

1. ~~Görünen pitch ile gerçek yükseliş aynı işaretli mi?~~ Evet: oyunda gösterilen pitch topun
   yönünden bağımsız, yukarı bakınca pozitif, aşağı bakınca negatif (`getDisplayPitch` doğrudan `cannonPitch` döndürüyor;
   bu değer yönden bağımsız). Sequenced Gearshift'te yön, girişin dönüş yönüne göre forward/reverse seçilmeli.
2. ~~Model gerçek atışla tutuyor mu?~~ Evet, yukarıdaki tablo.
