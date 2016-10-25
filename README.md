# BSiUI2
## Bezpieczeństwo Systemów i Usług Informatycznych 2 - laboratorium

### Laboratorium nr 1: Komunikator z szyfrowaniem

Komunikator w architekturze klient-serwer wspierający bezpieczną wymianę sekretu (protokół Diffiego-Hellmana) 
oraz obsługujący zadany format komunikacji JSON. Treść wiadomości zostaje zakodowana za pomocą Base64 przed umieszczeniem jej 
w strukturze JSON. Wspierane metody szyfrowania to szyfr Cezara, jednobajtowe szyfrowanie XOR lub brak szyfrowania.

### Wymagania systemowe:
Do projektu dołączono dwa wygenerowane jary wraz z zależnościami. Do ich uruchomienia potrzebna jest Java w wersji 1.8.*.

### Sposób uruchomenia:

### Serwer
- numer portu np:
`java -cp server.jar server.Server 1002`

### Klient
- nazwa hosta serwera,
- numer portu na którym serwer ma nasłuchiwać,
- nazwa klienta,
- rodzaj szyfrowania np:
`java -cp client.jar client.Client localhost 1002 Ala cezar`

