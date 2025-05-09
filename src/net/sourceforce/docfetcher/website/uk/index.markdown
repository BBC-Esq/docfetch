**Note**: You may be interested in DocFetcher Pro, the commercial big brother of DocFetcher with more features and fewer bugs, or DocFetcher Server, the commercial cousin of DocFetcher with multi-user support and a web interface. [Find out more](https://docfetcherpro.com/).

Опис
===========
DocFetcher є настільним відкрито-джерельним застосунком пошуку: Він дозволяє вам шукати вміст файлів на вашому комп'ютері. &mdash; Ви можете думати про нього, як про Google для ваших локальних файлів.. Цей застосунок працює на Windows, Linux та OS&nbsp;X, і доступний під [Eclipse Public License](https://en.wikipedia.org/wiki/Eclipse_Public_License).

Базове використання
===========
Екранознімок нижче показує головний інтерфейс користувача. Запити уводяться у текстове поле (1). Результати пошуку показуються у панелі результатів (2). Панель передогляду (3) показує лише текстовий передогляд файлу, що поточно вибраний у панелі результатів. Усі відповідності у цьому файлі підсвічуються жовтим.

Ви можете фільтрувати результати за мінімумом та/або максимумом розміру файлу (4), за типом файлу (5) та за розміщенням (6). Кнопки (7) використовуються для відкриття цього підручника, відкриття уподобань та мінімізування програми у системний трей, відповідно.

<div id="img" style="text-align: center;"><a href="../all/intro-001-results-edited.png"><img style="width: 500px; height: 375px;" src="../all/intro-001-results-edited.png"></a></div>

DocFetcher вимагає, щоб ви створили так звані *індекси* для тек, в яких ви хочете шукати. Що таке індексування та як воно працює, пояснюється більш детально нижче. У кількох словах, індекс дозволяє DocFetcher'у знаходити дуже швидко (за кілька мілісекунд) те, які файли містять певний набір слів, таким чином значно пришвидшуючи пошуки. Наступний екранознімок показує діалог DocFetcher'а для створення нових індексів:

<div id="img" style="text-align: center;"><a href="../all/intro-002-config.png"><img style="width: 500px; height: 375px;" src="../all/intro-002-config.png"></a></div>

Клацання на кнопці "Пуск" внизу справа цього діалогу запускає індексування. Процес індексування може займати деякий час, залежно від кількості та розмірів файлів, що індексуються. Хороше емпіричне правило - 200 файлів за хвилину.

Хоча створення індексу займає деякий час, воно робиться лише один раз для кожної теки. Також, *оновлення* індексу після того, як вміст теки змінено, набагато швидше, ніж його створення &mdash; перше зазвичай займає лише кілька секунд.

Помітні особливості
================
* **Портативна версія**: Існує портативна версія DocFetcher'а, що працює на Windows, Linux *та* OS&nbsp;X. Якщо це корисно, описується більш детально далі нижче на цій сторінці.
* **Підтримка 64-біт**: Підтримуються обидві 32-біт та 64-біт операційні системи.
* **Підтримка Unicode**: DocFetcher поставляється з міцною підтримкою Unicode для усіх головних форматів, включаючи Microsoft Office, OpenOffice.org, PDF, HTML, RTF та файли звичайного тексту.
* **Підтримка архівів**: DocFetcher підтримує наступні формати архівів: zip, 7z, rar та все сімейство tar.*. Розширення файлів для архівів zip можна налаштовувати, що дозволяє вам додавати ще більше базованих на zip форматів архівів при потребі. Також, DocFetcher може обробляти безмежне вкладення архівів (наприклад, архів zip містить у собі архів 7z, котрий містить архів rar... і так далі).
* **Пошук у файлах джерельного коду**: Розширення файлів, за якими DocFetcher розпізнає файли зі звичайним текстом, можуть бути налаштовані, звідси ви можете використовувати DocFetcher для пошуку у будь-якому виді джерельного коду та інших базованих на тексті форматах файлів. (Це працює досить добре у комбінації з налаштовуваними розширеннями для zip, наприклад, для пошуку джерельному коді Java всередині файлів Jar.)
* **Файли Outlook PST**: DocFetcher дозволяє пошук в е-листах Outlook, які Microsoft Outlook типово збергіає у файлах PST.
* **Виявлення пар HTML**: Стандартно, DocFetcher виявляє пари файлів HTML (наприклад, файл з іменем "foo.html" та теку з назвою "foo_files"), та обробляє цю пару, як єдиний документ. Ця особливість може здаватися спочатку некорисною, але виявляється, що це значно підвищує якість результатів пошуку, коли ви маєте справу з файлами HTML, оскільки увесь "безлад" всередині тек HTML зникає з результатів.
* **Базоване на регулярних виразах виключення файлів з індексування**: Ви можете використовувати регулярні вирази для виключення певних файлів з індексування. Наприклад, для виключення файлів Microsoft Excel, ви можете вжити такий регулярний вираз: `.*\.xls`
* **Виявлення типу mime**: Ви можете використовувати регулярні вирази для "виявлення типу mime" для певних файлів, тобто DocFetcher буде намагатися виявляти їх фактичні файлові типи не просто дивлячись на ім'я файлу, але також заглядаючи у вміст цих файлів. Це стає в нагоді для файлів, що мають неправильне файлове розширення.
* **Потужний синтаксис запиту**: Додатково до базових конструкцій, як `OR`, `AND` та `NOT`, DocFetcher також підтримує серед інших: знаки підстанови, пошук фраз, нечіткий пошук ("знайти слова, що є подібними до..."), пошук за близькістю ("ці два слова повинні бути на відстані максимум 10 слів одне від одного"), підвищення пріоритету ("підвищити пріоритет документів, що містять...")

Підтримувані формати документів
==========================
* Microsoft Office (doc, xls, ppt)
* Microsoft Office 2007 і новіше (docx, xlsx, pptx, docm, xlsm, pptm)
* Microsoft Outlook (pst)
* OpenOffice.org (odt, ods, odg, odp, ott, ots, otg, otp)
* Portable Document Format (pdf)
* EPUB (epub)
* HTML (html, xhtml, ...)
* TXT та інші формати звичайного тексту (налаштовувано)
* Rich Text Format (rtf)
* AbiWord (abw, abw.gz, zabw)
* Microsoft Compiled HTML Help (chm)
* MP3 Metadata (mp3)
* FLAC Metadata (flac)
* JPEG Exif Metadata (jpg, jpeg)
* Microsoft Visio (vsd)
* Scalable Vector Graphics (svg)

Порівняння з іншими настільними застосунками пошуку
===============================================
У порівнянні з іншими настільними застосунками пошуку DocFetcher виділяється у наступному:

**Свобода від безладу**: Ми прагнемо зберігати інтерфейс користувача в DocFetcher'і вільним від хаосу та безладу. Жодних спливних вікон з рекламою та "не хотіли б ви зареєструватися...?" Жодного безкорисного мотлоху не інсталюється у вашому веб браузері, реєстрі або будь-де у вашій системі.

**Конфіденційність**: DocFetcher не збирає ваші особисті дані. Ніколи. Будь-хто, хто сумнівається в цьому, може перевірити загальнодоступний [джерельний код](https://docfetcher.sourceforge.net/wiki/doku.php?id=source_code).

**Безкоштовно назавжди**: Оскільки DocFetcher є відкрито-джерельним Open Source, ви не маєте турбуватися про те, що програма коли-небудь стане застарілою або непідтримуваною, тому що джерельний код завжди буде доступний. Говорячи про підтримку, чи отримували ви новини, що Google Desktop, один з головних комерційних конкурентів DocFetcher'а, було припинено у 2011? Ну ось...

**Крос-платформність**: На відміну від багатьох своїх конкурентів, DocFetcher не тільки виконується на Windows, але також і на Linux та OS&nbsp;X. Таким чином, якщо ви хочете відійти від свого Windows та перейти на Linux чи OS&nbsp;X, DocFetcher буде чекати вас на іншій стороні.

**Портативність**: Однією з найбільших переваг DocFetcher'а є його портативність. Базово, з DocFetcher ви можете вибудувати завершений, повністю здатний для пошуку репозиторій документів та перенести його на ваш диск USB. Більше про це у наступному підрозділі.

**Індексування лише того, що вам потрібно**: Серед комерційних конкурентів DocFetcher'а, здається, є тенденція підштовхування користувачів до індексування всього жорсткого диска &mdash; можливо, у спробі відібрати якомога більше рішень від нібито "німих" користувачів, або, що ще гірше, у спробі збирати більше даних користувача. Однак на практиці можна вважати, що більшість людей *не* хочуть індексувати весь жорсткий диск: Це не тільки втрата часу на індексацію та дискового простору, але й захаращує результати пошуку небажаними файлами. Звідси, DocFetcher індексує лише теки, які ви явно хочете індексувати, а також надаєте безліч опцій фільтрації.

Портативні репозиторії документів
==============================
Однією з видатних особливостей DocFetcher'а є доступність портативної версії, яка дозволяє вам створювати *портативний репозиторій документів* &mdash; повністю індексований та повністю здатний до пошуку репозиторій всіх ваших важливих документів, котрий ви можете вільно переміщати.

**Приклади використання**: Існують різні речі, які ви можете робити з таким репозиторієм: Ви можете носити його з собою на диску USB, записати на CD-ROM з метою архівування, покласти його на зашифрований дисковий том (рекомендується: [TrueCrypt](https://www.truecrypt.org/)), синхронізувати його між кількома комп'ютерами через хмарне сховище, як [DropBox](https://www.dropbox.com/) тощо. Ще краще, оскільки DocFetcher є Open Source, ви можете навіть розповсюджувати ваш репозиторій: вивантажте його та поділіться ним з рештою світу, якщо хочете.

**Java: Продуктивність та портативність**: Одним з аспектів, з яким можуть зіткнутися деякі люди, є те, що DocFetcher був написаний на Java, який має репутацію бути "повільною". Це дійсно було вірно десять років тому, але з того часу продуктивність Java помітно покращилася, [згідно з Wikipedia](https://en.wikipedia.org/wiki/Java_%28software_platform%29#Performance). У будь-якому випадку, велика річ про те, що написано на Java, це те, що той самий портативний пакет DocFetcher'а може виконуватися на Windows, Linux *та* OS&nbsp;X &mdash; багато інших програм вимагають використання окремих пакетів для кожної платформи. Як результат, можна, наприклад, перенести свій портативний репозиторій документів на диск USB, а потім отримати доступ до нього з *будь-якої* з цих операційних систем, за умови, що інстальовано виконуване середовище Java.

Як працює індексування
==================
Цей підрозділ намагається дати базове розуміння, що таке індексування та як воно працює.

**Наївний підхід до пошуку файлів**: Найбільш базовий підхід до пошуку файлів є просто відвідати кожен файл у певній локації один за одним при здійсненні шукання. Це працює досить добре для пошуку *лише за іменами файлів*, оскільки аналізування імен файлів є дуже швидким. Проте, це не працюватиме так добре, якщо ви хочете шукати *вміст* файлів, оскільки витягнення повного тексту є набагато більш витратною операцією, ніж аналіз імен файлів.

**Базований на індексуванні пошук**: Ось чому DocFetcher, будучи шукачем вмісту, застосовує підхід, відомий як *індексування*: Базова ідея полягає у тому, що більшість файлів, в яких людям потрібно шукати (схоже, більше ніж 95%), модифікуються дуже рідко або взагалі не змінюються. Тому, чим робити повне витягнення тексту з кожного файлу при кожному пошуку, набагато більш ефективно здійснювати витягнення тексту з усіх файлів лише *один раз* та створити так званий *індекс* з усього витягненого тексту. Цей індекс є певним видом словника, що дозволяє швидко шукати файли за словами, які вони містять.

**Аналогія з телефонною книгою**: Як аналогія, подумайте, наскільки ефективніше шукати чийсь телефонний номер у телефонній книзі ("індекс"), замість того, щоб телефонувати на *кожен* можливий номер телефону лише для того, щоб дізнатися, чи є потрібна людина на іншому кінці, яку ви шукаєте. &mdash; Зателефонувати комусь по телефону і витягти текст з файлу можна вважати "витратними операціями". Також, той факт, що люди не дуже часто змінюють свої телефонні номери, аналогічний тому, що більшість файлів на комп'ютері рідко коли-небудь змінюються.

**Оновлення індексу**. Звичайно, індекс лише відбиває стан індексованих файлів, коли він був створений, і не обов'язково останній стан файлів. Звідси, якщо індекс не оновлюється, можна отримати застарілі результати пошуку, так само, як телефонна книга може стати застарілою. Однак, це не повинно бути проблемою, якщо ми можемо припустити, що більшість файлів рідко змінюються. Додатково, DocFetcher здатний *автоматично* оновлювати свої індекси: (1) Коли він працює, він виявляє змінені файли і відповідно оновлює свої індекси. (2) Коли він не запущений, маленький демон у фоновому режимі виявить зміни та збереже список індексів для оновлення; DocFetcher потім оновить ці індекси під час наступного запуску. І не турбуйтеся про цей демон: він має дуже низький рівень завантаження CPU і пам'яті, оскільки не робить нічого, окрім того, що помічає, які папки змінилися, і залишає більш витратні оновлення індексів для DocFetcher.
