# Bedwars - un capolavoro di Jacopo Parenti

Questo mio progetto è un plugin per server [**Minecraft**](https://www.minecraft.net/it-it) che aggiunge una modalità di gioco, _Bedwars_.

## Cos'è?
Bedwars è una modalità di gioco di Minecraft dove per vincere bisogna distruggere i letti degli avversari, mentre si protegge il proprio.

![L'isola iniziale del gioco](https://i.ibb.co/chfnVFk/immagine.png)

Senza di esso, la morte comporta la perdita della partita.
(TODO gif rottura letti)

Durante la partita, i giocatori possono interagire con il negozio per comprare oggetti, potenziamenti e blocchi per raggiungere gli altri giocatori.
(TODO foto shop + upgrades, gif per le categorie + details)



## Com'è stato fatto?
Il plugin è stato fatto in Java, nell'ambiente di sviluppo IntelliJ IDEA Ultimate.\
Per eseguirlo, mi sono appoggiato a [PaperMC](https://papermc.io), una fork del [server Minecraft](https://www.minecraft.net/it-it/download/server) con funzionalità aggiuntive come il supporto dei plugin.\
Tutti i dati del plugin sono memorizzati in un [file](https://www.mediafire.com/file/ecpjo9grd2yicjh/db.sql/file) .sql, gestito con SQLite.

### Librerie utilizzate:

- [JDK 21](https://docs.oracle.com/en/java/javase/21/docs/api/index.html)
- [PaperSpigot](https://jd.papermc.io/paper/1.20/), per tutto ciò che riguarda il server Minecraft
- [Citizens](https://jd.citizensnpcs.co/), per interagire con gli "NPC", entità immobili con lo scopo di reindirizzare il giocatore a una partita
- [Apache Commons IO](https://commons.apache.org/proper/commons-io/apidocs/), per semplificare delle operazioni relative ai file

## Requisiti:

- [Java 21](https://www.oracle.com/it/java/technologies/downloads/#java21)
- [Paper 1.20](https://papermc.io/downloads/paper)
- [Citizens](https://ci.citizensnpcs.co/job/Citizens2/)

<details>
<summary>Struttura classi</summary>
  <details>
    <summary>org.bedwars - contiene la classe base Bedwars</summary>
    <details>
      <summary>chat - logica per i messaggi inviati nella chat di gioco</summary>
      commands - comando /shout, che invia un messaggio a tutti i giocatori<br>
      listeners - per modificare i messaggi inviati
    </details>
    config - tutte le classi contenenti informazioni sulle mappe di gioco, costanti, negozi, e altro
    <details>
      <summary>game - logica di gioco</summary>
      commands - comando /forcestart, che inizia una partita anche se non ci sono abbastanza giocatori<br>
      listeners - per catturare tutti gli eventi di gioco, per esempio un blocco piazzato o un letto distrutto<br>
      <details>
        <summary>shop - per gestire il negozio</summary>
        listeners -  eventi relativi ai negozi
      </details>
      general - comandi e eventi globali
      inventories - eventi relativi agli inventari di gioco
      lobby - 
    </details>
  </details>
</details>
