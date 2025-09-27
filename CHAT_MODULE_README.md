# Module Chat Instantané - Documentation

## Vue d'ensemble

Ce module de chat instantané a été conçu pour être **modulaire et réutilisable**. Il peut être facilement extrait de ce projet et intégré dans d'autres applications Spring Boot.

## Architecture

### Structure du module

```
com._projects.internship/
├── model/chat/
│   ├── Conversation.java       # Entité pour les conversations
│   └── Message.java            # Entité pour les messages
├── dto/chat/
│   ├── ConversationDTO.java    # DTO pour les conversations
│   ├── MessageDTO.java         # DTO pour les messages
│   ├── SendMessageRequest.java # Requête d'envoi de message
│   └── UpdateMessageRequest.java # Requête de mise à jour
├── repository/chat/
│   ├── ConversationRepository.java # Repository des conversations
│   └── MessageRepository.java      # Repository des messages
├── service/chat/
│   ├── ChatService.java           # Interface principale du service
│   ├── ChatServiceImpl.java       # Implémentation du service
│   ├── ChatParticipantService.java # Gestion des participants
│   └── ChatParticipantServiceImpl.java
├── controller/chat/
│   ├── ChatRestControllerV2.java  # Endpoints REST
│   └── ChatWebSocketController.java # Contrôleur WebSocket
├── config/
│   ├── ChatWebSocketConfig.java   # Configuration WebSocket
│   └── ChatWebSocketEventListener.java # Gestionnaire d'événements
└── exception/
    ├── ResourceNotFoundException.java
    └── UnauthorizedException.java
```

## Fonctionnalités

### 1. Gestion des Messages
- ✅ Envoi de messages en temps réel
- ✅ Modification de messages (dans les 24h)
- ✅ Suppression de messages (soft delete)
- ✅ Historique des modifications
- ✅ Support des réponses à un message

### 2. Gestion des Conversations
- ✅ Conversations directes (1-to-1)
- ✅ Pagination des messages
- ✅ Marquage comme lu
- ✅ Compteur de messages non lus
- ✅ Aperçu du dernier message

### 3. Gestion des Participants
- ✅ Règles métier personnalisables
- ✅ Vérification des autorisations
- ✅ Liste des participants éligibles

### 4. WebSocket / Temps Réel
- ✅ Messages en temps réel via STOMP
- ✅ Indicateur de frappe
- ✅ Statut en ligne/hors ligne
- ✅ Accusés de lecture
- ✅ Notifications de suppression

## Endpoints REST

### Messages

```http
POST /api/chat/v2/messages
Content-Type: application/json
{
  "recipientId": 123,
  "content": "Hello!",
  "type": "TEXT",
  "replyToId": null
}
```

```http
PUT /api/chat/v2/messages/{messageId}
Content-Type: application/json
{
  "content": "Updated message"
}
```

```http
DELETE /api/chat/v2/messages/{messageId}
```

### Conversations

```http
GET /api/chat/v2/conversations
?page=0&size=20&sort=lastMessageAt,desc
```

```http
GET /api/chat/v2/conversations/{userId}/messages
?page=0&size=50&sort=createdAt,desc
```

```http
PUT /api/chat/v2/conversations/{conversationId}/read
```

### Participants

```http
GET /api/chat/v2/participants
```

```http
GET /api/chat/v2/unread-count
```

## WebSocket

### Configuration
- Endpoint: `/ws-chat`
- Protocole: STOMP over WebSocket
- Fallback: SockJS

### Destinations

#### Client → Serveur (préfixe `/app`)
- `/app/chat.send` - Envoyer un message
- `/app/chat.typing` - Indicateur de frappe
- `/app/chat.connect` - Connexion
- `/app/chat.disconnect` - Déconnexion
- `/app/chat.read` - Accusé de lecture
- `/app/chat.delete` - Supprimer un message

#### Serveur → Client
- `/user/queue/messages` - Messages privés
- `/user/queue/typing` - Indicateur de frappe
- `/user/queue/deletions` - Notifications de suppression
- `/topic/presence` - Statut en ligne/hors ligne
- `/topic/conversation/{id}/read` - Accusés de lecture

## Règles Métier

### Autorisations de Chat (personnalisables)

Par défaut, le module implémente ces règles :
- **Étudiants** : peuvent discuter uniquement avec les entreprises auxquelles ils ont postulé
- **Entreprises** : peuvent discuter uniquement avec les étudiants qui ont postulé à leurs offres
- **Admins** : peuvent discuter avec tout le monde (optionnel)

Ces règles sont définies dans `ChatParticipantServiceImpl` et peuvent être facilement modifiées.

## Installation dans un Autre Projet

### 1. Copier les fichiers

Copiez tous les fichiers du module chat dans votre projet.

### 2. Dépendances Maven/Gradle

Assurez-vous d'avoir ces dépendances :

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot WebSocket -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Lombok (optionnel mais recommandé) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

### 3. Configuration

Ajoutez dans `application.yml` :

```yaml
api:
  prefix: /api

spring:
  jpa:
    hibernate:
      ddl-auto: update
```

### 4. Adapter les Règles Métier

Modifiez `ChatParticipantServiceImpl` selon vos besoins :

```java
@Override
public boolean canUsersChat(User user1, User user2) {
    // Implémentez vos propres règles ici
    return true; // Exemple : tout le monde peut discuter
}
```

### 5. Intégration avec votre Modèle User

Le module s'attend à avoir une entité `User` avec au minimum :
- `Long id`
- `String username`
- `String firstName`
- `String lastName`
- `Role role`

## Sécurité

### Points d'Attention

1. **CORS** : Configurez les origines autorisées dans `ChatWebSocketConfig`
2. **Authentification** : Le module utilise Spring Security
3. **Validation** : Toutes les entrées sont validées avec Jakarta Validation
4. **Autorisations** : Vérification systématique des droits

### Recommandations

- Utilisez HTTPS en production
- Configurez un rate limiting sur les endpoints
- Implémentez un système de modération si nécessaire
- Ajoutez un antispam pour les messages

## Extension du Module

### Ajouter le Support des Groupes

1. Modifier `Conversation.ConversationType` pour ajouter `GROUP`
2. Adapter les règles dans `ChatParticipantService`
3. Ajouter des endpoints pour la gestion des groupes

### Ajouter les Fichiers/Images

1. Étendre `Message.MessageType` avec `IMAGE`, `FILE`
2. Ajouter un service de stockage de fichiers
3. Modifier `SendMessageRequest` pour accepter les fichiers

### Ajouter la Recherche

Implémentez la méthode `searchMessages` dans `ChatServiceImpl` :

```java
@Override
public Page<MessageDTO> searchMessages(User user, String query, Pageable pageable) {
    // Utilisez Elasticsearch ou une recherche full-text PostgreSQL
}
```

## Tests

Pour tester le module :

```bash
# Compiler le backend
mvn clean compile

# Lancer les tests
mvn test
```

## Support

Pour toute question sur l'intégration ou l'utilisation du module, consultez les fichiers sources qui sont bien documentés avec des JavaDoc.

## Licence

Ce module est fourni tel quel et peut être utilisé librement dans vos projets.
