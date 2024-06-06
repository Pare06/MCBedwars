# Bedwars - un capolavoro di Jacopo Parenti

Questo mio progetto è un plugin per server [**Minecraft**](https://www.minecraft.net/it-it) che aggiunge una modalità di gioco, _Bedwars_.

## Cos'è?
Bedwars è una modalità di gioco di Minecraft dove per vincere bisogna distruggere i letti degli avversari, mentre si protegge il proprio.

![L'isola iniziale del gioco](https://i.ibb.co/chfnVFk/immagine.png)

Senza di esso, la morte comporta la perdita della partita.
(TODO gif rottura letti)

## Com'è stato fatto?
Il plugin è stato fatto in Java, nell'ambiente di sviluppo IntelliJ IDEA Ultimate.\
Per eseguirlo, mi sono appoggiato a [PaperMC](https://papermc.io), una fork del [server Minecraft](https://www.minecraft.net/it-it/download/server) con funzionalità aggiuntive come il supporto dei plugin.\
Tutti i dati del plugin sono memorizzati in un file .sql, gestito con SQLite.

## Funzionalità
Durante la partita, i giocatori possono interagire con il negozio per comprare oggetti, potenziamenti e blocchi per raggiungere gli altri giocatori.
![](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExdWh3MHN1NWhnaTYycGZlMGoyc2tzcHV4Y2ltc3dldnMzdnJnbTh4NSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/qqRPRyEQ9b41ghnadb/giphy-downsized-large.gif)

Oltre alle isole dei giocatori, al centro della mappa sono presenti altre isole secondarie dove vengono generati diamanti e smeraldi, che possono essere usati per acquistare potenziamenti.
(TODO gif spawners)

Eseguendo azioni particolari, come uccidere una squadra intera da soli oppure vincendo un certo numero di partite si sbloccano gli "achievements",
ovvero dei titoli che permettono di (TODO che metto?)
(TODO gif schermata achievements)

Durante la partita, a intervalli regolari la mappa riceverà delle modifiche che rendono più veloce la partita,
e dopo 75 minuti dall'inizio della partita verranno distrutti tutti i letti dei giocatori, per permettere di finire in fretta la partita.
(TODO gif countdown upgrades)

Ad ogni uccisione, letto distrutto o partita vinta si ottengono punti esperienza, che permettono di salire di livello.
(TODO gif level up)

Quando un giocatore senza un letto viene ucciso, verrà considerato eliminato ma potrà comunque vedere l'andamento della partita, senza interferire.
(TODO gif spect)

Se un giocatore dovesse perdere la connessione, può riconnettersi ed essere sempre considerato in gioco, a meno che il suo letto non sia stato distrutto.
(TODO gif rejoin)

### Librerie utilizzate:

- [JDK 21](https://docs.oracle.com/en/java/javase/21/docs/api/index.html)
- [PaperSpigot](https://jd.papermc.io/paper/1.20/), per tutto ciò che riguarda il server Minecraft
- [Citizens](https://jd.citizensnpcs.co/), per interagire con gli "NPC", entità immobili con lo scopo di reindirizzare il giocatore a una partita
- [Apache Commons IO](https://commons.apache.org/proper/commons-io/apidocs/), per semplificare delle operazioni relative ai file
- [Adventure](https://javadoc.io/doc/net.kyori/adventure-api/latest/index.html), per la gestione dei messaggi

### Requisiti:

- [Java 21](https://www.oracle.com/it/java/technologies/downloads/#java21)
- [Paper 1.20](https://papermc.io/downloads/paper)
- [Citizens](https://ci.citizensnpcs.co/job/Citizens2/)
