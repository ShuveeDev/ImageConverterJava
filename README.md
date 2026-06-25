# ImageConverterJava

Простий конвертер зображень на Java з сучасним графічним інтерфейсом (GUI) та консольним CLI режимом.

## Вимоги
- Java 11 або новіша

## Збірка JAR
```bash
./build.sh
```
Буде створено `ImageConverter.jar` в корені проекту.

## Використання

### 1. Графічний інтерфейс (GUI)
Щоб запустити зручне графічне вікно (Dark Mode), просто запустіть JAR без параметрів або з прапором `--gui`:
```bash
java -jar ImageConverter.jar
# або
java -jar ImageConverter.jar --gui
```

### 2. Консольний режим (CLI)

Конвертувати один файл (зберігає поруч з оригіналом):
```bash
java -jar ImageConverter.jar photo.png jpg
```

Конвертувати з власною назвою / шляхом:
```bash
java -jar ImageConverter.jar photo.png jpg /path/to/output.jpg
```

Налаштувати якість JPEG (від 0 до 100):
```bash
java -jar ImageConverter.jar photo.png jpg --quality 80
```

Конвертувати папку цілком (пакетний режим):
```bash
java -jar ImageConverter.jar --batch ./images png ./output_folder
```

## Підтримувані формати
`png`, `jpg`, `jpeg`, `bmp`, `gif`, `webp`, `tiff`.
 При конвертації прозорого фону (PNG) в непрозорі формати (JPG, BMP), прозорі ділянки автоматично заливаються білим кольором.
