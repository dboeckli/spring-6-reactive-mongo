# customer Flow: createCustomer → getCustomer → deleteCustomer

Datei: `restRequests/customer-flow-create-get-delete/customer-flow-create-get-delete.http`

## Ziel

Einen neuen Kunden anlegen, die erzeugte Ressource per GET prüfen und anschließend wieder löschen.

## Ablauf (Services)

```mermaid
sequenceDiagram
    actor C as HTTP Client (IntelliJ)
    participant API as spring-6-reactive-mongo
    participant DB as MongoDB (Reactive)

    C->>API: POST /api/v3/customer
    API->>DB: insert customer
    API-->>C: 201 (Location: /api/v3/customer/{customerId})

    C->>API: GET /api/v3/customer/{customerId}
    API->>DB: find customer
    API-->>C: 200 (created customer)

    C->>API: DELETE /api/v3/customer/{customerId}
    API->>DB: delete customer
    API-->>C: 204 (No Content)
```

## Requests (in dieser Reihenfolge)

1. `createCustomer` – legt den Kunden an und liest `customerId` aus dem `Location`-Header.
2. `getCustomerById` – prüft, dass der Kunde existiert und `customerName` = `Flow Customer` ist.
3. `deleteCustomer` – löscht den zuvor erzeugten Kunden (hinterlässt keine Testdaten).

## Benötigte Globals

|       Global        |         gesetzt von         |             genutzt von             |
|---------------------|-----------------------------|-------------------------------------|
| `customerId`        | `createCustomer`            | `getCustomerById`, `deleteCustomer` |
| `traceId`, `spanId` | `../scripts/traceparent.js` | alle Requests                       |

## Asserts

- `createCustomer` → `201` mit `Location`-Header.
- `getCustomerById` → `200`, `customerName` = `Flow Customer`.
- `deleteCustomer` → `204`.

## Hinweise

- Basis-URL ist `{{application-port}}` (Profil `local` = 8083, `k8s` = 30083).
- Der `Location`-Header wird als relativer Pfad `/api/v3/customer/{id}` erzeugt; die ID wird über das letzte
  Pfadsegment extrahiert.
- `customerName` ist Pflicht (`@NotBlank`, 3–255 Zeichen). Die Mongo-`_id` wird als String (ObjectId) zurückgegeben.
- Der Flow ist wiederholbar: Der angelegte Kunde wird am Ende gelöscht.
- Alle Requests benötigen ein Bearer-Token (`auth-id` aus `http-client.env.json`); dafür muss der Auth-Server auf
  Port `9000` laufen (siehe `docker compose up`).

