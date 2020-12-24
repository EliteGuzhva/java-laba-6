# Java: Лабораторная работа №6
## Описание
Приложение ставит штамп на титульной странице заданного PDF файла и сохраняет результат в новый файл
## Запуск
```bash
mkdir build
ant build
java -jar build/jar/pdfstamp.jar original.pdf stamp.png output.pdf
```