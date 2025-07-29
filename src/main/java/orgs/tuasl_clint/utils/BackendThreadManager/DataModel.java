package orgs.tuasl_clint.utils.BackendThreadManager;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import orgs.tuasl_clint.client.*;
import orgs.tuasl_clint.controllers.*;
import orgs.tuasl_clint.models2.*;
import orgs.tuasl_clint.models2.FactoriesSQLite.*;
import orgs.tuasl_clint.protocol.Response;
import orgs.tuasl_clint.utils.DiagnosticLogger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataModel implements
        OnAllUsersRetrievedListener,
        OnChatParticipantsRetrievedListener,
        OnChatRetrievedListener,
        OnCommandResponseListener,
        OnConnectionFailureListener,
        OnContactsRetrievedListener,
        OnMessagesRetrievedListener,
        OnNewMessageListener,
        OnNotificationsRetrievedListener,
        OnStatusUpdateListener,
        OnUserChatsRetrievedListener,
        OnUserRetrievedListener {
    private static final String LOG_FILE = "data_model_diagnostics.log";
    private static DataModel instance;
    private final MapProperty<Long, ObjectProperty<User>> users =
            new SimpleMapProperty<>(FXCollections.observableHashMap());
    private final MapProperty<Long, ObjectProperty<Chat>> chats =
            new SimpleMapProperty<>(FXCollections.observableHashMap());
    private final MapProperty<Long, ObjectProperty<ChatProperties>> chatProperties =
            new SimpleMapProperty<>(FXCollections.observableHashMap());
    private final Map<Long, Media> mediaMap = new ConcurrentHashMap<>();
    private final Map<String, PerformanceStats> performanceStats = new ConcurrentHashMap<>();

    private DataModel() {
        DiagnosticLogger.initializeLogger(LOG_FILE);
        DiagnosticLogger.log(LOG_FILE, "DataModel initialized");

        ChatClient.getInstance().addOnAllUsersRetrievedListener(this);
        ChatClient.getInstance().addOnChatParticipantsRetrievedListener(this);
        ChatClient.getInstance().addOnChatRetrievedListener(this);
        ChatClient.getInstance().addOnCommandResponseListener(this);
        ChatClient.getInstance().addOnConnectionFailureListener(this);
        ChatClient.getInstance().addOnContactsRetrievedListener(this);
        ChatClient.getInstance().addOnMessagesRetrievedListener(this);
        ChatClient.getInstance().addOnNewMessageListener(this);
        ChatClient.getInstance().addOnNotificationsRetrievedListener(this);
        ChatClient.getInstance().addOnStatusUpdateListener(this);
        ChatClient.getInstance().addOnUserChatsRetrievedListener(this);
        ChatClient.getInstance().addOnUserRetrievedListener(this);

        getDataFromDatabase();
        getDataFromServer();


    }
    private void getDataFromDatabase(){
        UserFactory.getAll().forEach(this::onUserRetrieved);
        ChatFactory.getAll().forEach(this::onChatRetrieved);
        onChatParticipantsRetrieved(ChatParticipantFactory.getAll());
        MessageFactory.getAll().forEach(this::onNewMessageReceived);
        MediaFactory.getAll().forEach(this::addMedia);
    }
    private void getDataFromServer(){
        soutt("getDataFromServer : "+ChatClient.getInstance().getUserChats().toString());
        chats.values().forEach(chatObjectProperty -> getChatPropertiesFromServer(chatObjectProperty.get()));
    }

    private void getChatPropertiesFromServer(Chat chat) {
        int chatId = ((int) chat.getId());
        soutt("getDataFromServer : "+ChatClient.getInstance().getChatParticipants(chatId).toString());
        soutt("getDataFromServer : "+ChatClient.getInstance().getChatMessages(chatId,100,0).toString());
        soutt("Process Data Of Chat Map(KEY:"+chat+",VALUE:"+chat.toString()+").....");
        getMessages(chat.getId()).values().forEach(message -> {
            Response response =  ChatClient.getInstance().getUserById(message.get().getSenderId().intValue());
            soutt("getDataFromServer : "+ response.toString());
            if(response.isSuccess())
                message.get().setSenderName(users.get(message.get().getSenderId()).get().getFirstName());
        });
    }

    public static synchronized DataModel getInstance() {
        if (instance == null) {
            instance = new DataModel();
        }
        return instance;
    }

    @Override
    public void onAllUsersRetrieved(List<User> users) {
        Platform.runLater(()->addUsers(users));
    }

    @Override
    public void onChatParticipantsRetrieved(List<ChatParticipant> participants) {
        Platform.runLater(()->participants.forEach(this::addOrUpdateChatParticipant));
    }

    @Override
    public void onChatRetrieved(Chat chat) {
        soutt("Chat Retreved: "+chat.toString());
        Platform.runLater(()->addChat(chat));
    }

    @Override
    public void onCommandResponse(Response response) {
        soutt("response Retreved: "+response.toString());
    }

    @Override
    public void onConnectionFailure(String errorMessage) {
        soutt("Error On Connection Server : "+errorMessage);
    }

    @Override
    public void onContactsRetrieved(List<User> contacts) {
        soutt("Contact Retreived : "+ contacts.toString());
    }

    @Override
    public void onMessagesRetrieved(List<Message> messages, int chatId) {
        soutt("Message Retreived : "+messages.toString());
        Platform.runLater(()->messages.forEach(this::onNewMessageReceived));
    }

    @Override
    public void onNewMessageReceived(Message message) {
        soutt("message Retreived : "+ message);
        Platform.runLater(()->{
            addMessageToChat(message);
        });
    }

    @Override
    public void onNotificationsRetrieved(List<Notification> notifications) {
        soutt("Notifications Retrived : "+notifications.toString());
    }

    @Override
    public void onStatusUpdate(String status) {
        soutt("Status Updated : "+status);
    }

    @Override
    public void onUserChatsRetrieved(List<Chat> chats) {
        soutt("New Chats Retrieved : "+chats.toString());
        Platform.runLater(()->addChats(chats));
    }

    @Override
    public void onUserRetrieved(User user) {
        Platform.runLater(()->addOrUpdateUser(user));
    }

    public MapProperty<Long, ObjectProperty<ChatProperties>> getChatProperties() {
        return chatProperties;
    }

    // Inner class for chat properties
    public static class ChatProperties {
        private final MapProperty<Long, ObjectProperty<Message>> messages =
                new SimpleMapProperty<>(FXCollections.observableHashMap());

        private final MapProperty<Long, ObjectProperty<ChatParticipant>> participants =
                new SimpleMapProperty<>(FXCollections.observableHashMap());

        private final MapProperty<Long, ObjectProperty<SendMessageItemController>> sentMessageControllers =
                new SimpleMapProperty<>(FXCollections.observableHashMap());

        private final ObjectProperty<ChatListItemController> chatListItemController =
                new SimpleObjectProperty<>();

        public MapProperty<Long, ObjectProperty<Message>> messagesProperty() { return messages; }
        public MapProperty<Long, ObjectProperty<ChatParticipant>> participantsProperty() { return participants; }
        public MapProperty<Long, ObjectProperty<SendMessageItemController>> sentMessageControllersProperty() { return sentMessageControllers; }
        public ObjectProperty<ChatListItemController> chatListItemControllerProperty() { return chatListItemController; }

        @Override
        public String toString() {
            return "ChatProperties{" +
                    "messages=" + messages.size() +
                    ", participants=" + participants.size() +
                    ", sentMessageControllers=" + sentMessageControllers.size() +
                    ", chatListItemController=" + (chatListItemController.get() != null) +
                    '}';
        }
    }

    // Performance tracking
    private static class PerformanceStats {
        long totalTime;
        int executionCount;
        long maxTime;

        void record(long duration) {
            totalTime += duration;
            executionCount++;
            if (duration > maxTime) maxTime = duration;
        }

        double averageTime() {
            return executionCount > 0 ? (double) totalTime / executionCount : 0;
        }

        @Override
        public String toString() {
            return String.format("Executions: %d, Avg: %.2f ms, Max: %d ms",
                    executionCount, averageTime(), maxTime);
        }
    }

    // Record performance metrics
    private void recordPerformance(String operation, long duration) {
        performanceStats.computeIfAbsent(operation, k -> new PerformanceStats())
                .record(duration);
    }

    // Generate performance report
    public void generatePerformanceReport() {
        StringBuilder report = new StringBuilder("DataModel Performance Report:\n");
        performanceStats.forEach((operation, stats) -> {
            report.append(String.format(
                    "%-40s: %s%n",
                    operation, stats.toString()
            ));
        });

        DiagnosticLogger.log(LOG_FILE, report.toString());
        soutt(report.toString());
    }
    // Controller creation tasks
    public static Task<SendMessageItemController> createSendMessageItemController(Message message) {
        return new Task<>() {
            @Override
            protected SendMessageItemController call() {
                ObjectProperty<Message> messageProperty;
                if (getInstance().chatProperties.containsKey(message.getChatId()) && getInstance().getMessages(message.getChatId()) != null) {
                    messageProperty = getInstance().getMessages(message.getChatId()).get(message.getId());
                }else {
                    messageProperty = new SimpleObjectProperty<>(message);
                }

                long startTime = System.currentTimeMillis();
                String operation = "Create SendMessageItemController";
                String details = "Message: " + messageProperty.get().toString();
                try {
                    // Simulate controller creation
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/sendMessageItem.fxml"));
                    Parent userCardNode = loader.load();
                    SendMessageItemController controller = loader.getController();
                    controller.setMessageData(messageProperty);
                    return controller;
                } catch (Exception e) {
                        serrr("Cannot Create Message List Item .... Error : " + e.getMessage());
                        serrr("Message Is : " + messageProperty.get().toString());

                    String errorMsg = operation + " failed: " + e.getMessage();
                    DiagnosticLogger.log(LOG_FILE, errorMsg);
                    serrr(errorMsg + " | Message: " + message.toString());
                    return null;
                } finally {
                    long duration = System.currentTimeMillis() - startTime;
                    DiagnosticLogger.logOperationDetails(
                            LOG_FILE,
                            operation,
                            details,
                            startTime
                    );
                    DataModel.getInstance().recordPerformance(operation, duration);
                }
            }
        };
    }
    public static Task<UserCardController> createUserCardControllerTask(@NotNull ObjectProperty<User> user) {
        return new Task<>() {
            @Override
            protected UserCardController call() {
                long startTime = System.currentTimeMillis();
                String operation = "Create User Card List Item";
                String details = "User : " + user.get().toString();
                try {
                    // Simulate controller creation
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/userCard.fxml"));
                    Parent userCardNode = loader.load();
                    UserCardController controller = loader.getController();
                    controller.setData(user);
                    return controller;
                } catch (Exception e) {
                    serrr("Cannot Create User List Item .... Error : " + e.getMessage());
                    serrr("Message Is : " + user.get().toString());
                    String errorMsg = operation + " failed: " + e.getMessage();
                    DiagnosticLogger.log(LOG_FILE, errorMsg);
                    serrr(errorMsg + " | User : " + user.get().toString());
                    return null;
                } finally {
                    long duration = System.currentTimeMillis() - startTime;
                    DiagnosticLogger.logOperationDetails(
                            LOG_FILE,
                            operation,
                            details,
                            startTime
                    );
                    DataModel.getInstance().recordPerformance(operation, duration);
                }
            }
        };
    }
    public static Task<AddParticipantController> createAddChatParticipantControllerTask(@NotNull int chat_id) {
        return new Task<>() {
            @Override
            protected AddParticipantController call() {
                long startTime = System.currentTimeMillis();
                String operation = "Create Add Participant View";
                String details = "Chat_id : " + chat_id;
                try {
                    // Simulate controller creation
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/orgs/tuasl_clint/fxml/AddParticipant.fxml"));
                    Parent userCardNode = loader.load();
                    AddParticipantController controller = loader.getController();
                    controller.setDataChatID(chat_id);
                    return controller;
                } catch (Exception e) {
                    serrr("Cannot Create Add Participant View .... Error : " + e.getMessage());
                    serrr("Chat_ID Is : " + chat_id);
                    String errorMsg = operation + " failed: " + e.getMessage();
                    DiagnosticLogger.log(LOG_FILE, errorMsg);
                    serrr(errorMsg + " | Chat_ID : " + chat_id);
                    return null;
                } finally {
                    long duration = System.currentTimeMillis() - startTime;
                    DiagnosticLogger.logOperationDetails(
                            LOG_FILE,
                            operation,
                            details,
                            startTime
                    );
                    DataModel.getInstance().recordPerformance(operation, duration);
                }
            }
        };
    }

    public static Task<ChatListItemController> createChatListItemControllerTask(Chat chat) {
        if(chat == null)
            return null;
        return new Task<>() {
            @Override
            protected ChatListItemController call() {
                ObjectProperty<Chat> chatProperty;
                if(getInstance().chats.containsKey(chat.getId()))
                    chatProperty = getInstance().getChatProperty(chat.getId());
                else
                    chatProperty = new SimpleObjectProperty<>(chat);

                long startTime = System.currentTimeMillis();
                String operation = "Create ChatListItemController";
                String details = "Chat ID: " + chatProperty.get().getId() + ", Name: " + chatProperty.get().getChatName();
                try {
                    // Simulate controller creation
                    FXMLLoader loader = new FXMLLoader((getClass().getResource("/orgs/tuasl_clint/fxml/chatListItem.fxml")));
                    Parent parent = loader.load();
                    ChatListItemController controller = loader.getController();
                    controller.setChat(chatProperty);
                    return controller;
                } catch (Exception e) {
                    String errorMsg = operation + " failed: " + e.getMessage();
                    DiagnosticLogger.log(LOG_FILE, errorMsg);
                    serrr(errorMsg + " | Chat: " + chatProperty.get().toString());
                    return null;
                } finally {
                    long duration = System.currentTimeMillis() - startTime;
                    DiagnosticLogger.logOperationDetails(
                            LOG_FILE,
                            operation,
                            details,
                            startTime
                    );
                    DataModel.getInstance().recordPerformance(operation, duration);
                }
            }
        };
    }

    // Media management
    public boolean addOrReplaceMediaForMessage(Media media, long messageId) {
        long startTime = System.currentTimeMillis();
        String operation = "Add/Replace Media for Message";
        String details = "Media ID: " + media.getId() + ", Message ID: " + messageId;
        try {
            boolean messageExists = false;
            for (Map.Entry<Long, ObjectProperty<ChatProperties>> entry : chatProperties.entrySet()) {
                ChatProperties props = entry.getValue().get();
                if (props != null && props.messages.containsKey(messageId)) {
                    messageExists = true;
                    // Update media in message
                    ObjectProperty<Message> messageProp = props.messages.get(messageId);
                    Message message = messageProp.get();
                    message.setMedia(media);
                    messageProp.set(message);
                    // Update media map
                    mediaMap.put(media.getId(), media);
                    break;
                }
            }

            if (!messageExists) {
                String errorMsg = "Message not found: " + messageId;
                DiagnosticLogger.log(LOG_FILE, errorMsg);
                serrr(errorMsg);
                return false;
            }
            return true;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            DiagnosticLogger.logOperationDetails(
                    LOG_FILE,
                    operation,
                    details,
                    startTime
            );
            recordPerformance(operation, duration);
        }
    }

    public boolean addMediaForMessage(Media media, long messageId) {
        long startTime = System.currentTimeMillis();
        String operation = "Add Media to Message";
        String details = "Media ID: " + media.getId() + ", Message ID: " + messageId;

        try {
            // Check if message exists in any chat
            boolean messageExists = false;
            for (Map.Entry<Long, ObjectProperty<ChatProperties>> entry : chatProperties.entrySet()) {
                ChatProperties props = entry.getValue().get();
                if (props != null && props.messages.containsKey(messageId)) {
                    messageExists = true;

                    // Check if message already has media
                    Message message = props.messages.get(messageId).get();
                    if (message.getMedia() != null) {
                        String warning = "Message already has media: " + messageId;
                        DiagnosticLogger.log(LOG_FILE, warning);
                        serrr(warning);
                        return false;
                    }

                    // Add media to message
                    message.setMedia(media);
                    props.messages.get(messageId).set(message);

                    // Add to media map
                    mediaMap.put(media.getId(), media);
                    break;
                }
            }

            if (!messageExists) {
                String errorMsg = "Message not found: " + messageId;
                DiagnosticLogger.log(LOG_FILE, errorMsg);
                serrr(errorMsg);
                return false;
            }

            return true;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            DiagnosticLogger.logOperationDetails(
                    LOG_FILE,
                    operation,
                    details,
                    startTime
            );
            recordPerformance(operation, duration);
        }
    }

    // Chat management
    public void addChat(Chat chat) {
        long startTime = System.currentTimeMillis();
        String operation = "Add/Update Chat";
        String details = "Chat ID: " + chat.getId() + ", Name: " + chat.getChatName();
        soutt("Adding Chat : "+chat);
        try {
            Long chatId = chat.getId();

            // Update existing chat or add new one
            if (chats.containsKey(chatId)) {
                // Update existing chat
                ObjectProperty<Chat> chatProp = chats.get(chatId);
                chatProp.set(chat);
                Executor.execute(chat::saveOrUpdate);
                soutt("Updated existing chat: " + chatId);
            } else {
                // Add new chat
                ObjectProperty<Chat> chatProperty = new SimpleObjectProperty<>(chat);
                chats.put(chatId, chatProperty);

                // Initialize chat properties
                ObjectProperty<ChatProperties> props = new SimpleObjectProperty<>(new ChatProperties());

                props.get().messages.addListener(new ChangeListener<ObservableMap<Long, ObjectProperty<Message>>>() {// Binding the new maps of message and message controller map
                    @Override
                    public void changed(ObservableValue<? extends ObservableMap<Long, ObjectProperty<Message>>> observableValue, ObservableMap<Long, ObjectProperty<Message>> oldMap, ObservableMap<Long, ObjectProperty<Message>> newMap) {
                        newMap.forEach((key,value)->{
                            if(!props.get().sentMessageControllers.containsKey(key)){
                                var task = DataModel.createSendMessageItemController(value.get());
                                task.setOnSucceeded(abc->{
                                    if(task.getValue() != null){
                                        props.get().sentMessageControllers.put(key,new SimpleObjectProperty<>(task.getValue()));
                                    }
                                    else
                                        soutt("Cannot Bind The messages map into messages controllers map using the value of message : "+value.get().toString());
                                });
                                Executor.submit(task);
                            }
                        });
                    }
                });

                chatProperties.put(chatId, props);
                Executor.execute(()->ChatClient.getInstance().getChatMessages(chatId.intValue(),50,0));
                // Create controller asynchronously
                Task<ChatListItemController> task = createChatListItemControllerTask(chat);
                task.setOnSucceeded(e -> {
                    ChatProperties cp = props.get();
                    if (cp != null) {
                        cp.chatListItemController.set(task.getValue());
                    }
                });
                Executor.submit(task);
                soutt("Added new chat: " + chatId);
            }
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            DiagnosticLogger.logOperationDetails(
                    LOG_FILE,
                    operation,
                    details,
                    startTime
            );
            recordPerformance(operation, duration);
        }
    }

    public void addChats(List<Chat> chatList) {
        long startTime = System.currentTimeMillis();
        String operation = "Add Multiple Chats";
        String details = "Chats count: " + chatList.size();

        try {
            for (Chat chat : chatList) {
                addChat(chat);
            }
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            DiagnosticLogger.logOperationDetails(
                    LOG_FILE,
                    operation,
                    details,
                    startTime
            );
            recordPerformance(operation, duration);
        }
    }

    public boolean removeChat(long chatId) {
        long startTime = System.currentTimeMillis();
        String operation = "Remove Chat";
        String details = "Chat ID: " + chatId;

        try {
            if (chats.containsKey(chatId)) {
                var v = chats.get(chatId).get();
                chats.remove(chatId);
                chatProperties.remove(chatId);
                soutt("Removed chat: " + chatId);
                v.delete();
                return true;
            }
            serrr("Chat not found for removal: " + chatId);
            return false;
        } catch (SQLException e) {
            serrr(e.getMessage());
            details+= " Error: "+e.getMessage();
            return false;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            DiagnosticLogger.logOperationDetails(
                    LOG_FILE,
                    operation,
                    details,
                    startTime
            );
            recordPerformance(operation, duration);
        }
    }

    public ObjectProperty<Chat> getChatProperty(long chatId) {
        return chats.get(chatId);
    }

    public MapProperty<Long,ObjectProperty<Chat>> getChats(){
        return chats;
    }

    public ObjectProperty<ChatListItemController> getChatListItemControllerProperty(long chatId) {
        ObjectProperty<ChatProperties> props = chatProperties.get(chatId);
        return props != null ? props.get().chatListItemController : null;
    }

    // Message management
    public MapProperty<Long, ObjectProperty<Message>> getMessages(long chatId) {
        ObjectProperty<ChatProperties> props = chatProperties.get(chatId);
        if (props != null && props.get() != null) {
            return props.get().messages;
        }
        return new SimpleMapProperty<>();
    }

    public void setNewMessageReceivedListener(OnNewMessageListener newMessageReceivedListener) {
        this.newMessageReceivedListener = newMessageReceivedListener;
    }

    public void addMessageToChat(Message message) {
        long startTime = System.currentTimeMillis();
        String operation = "Add Message to Chat";
        Long chatId = message.getChatId();
        String details = "Chat ID: " + chatId + ", Message ID: " + message.getId();
        try {
            ObjectProperty<ChatProperties> props = chatProperties.get(chatId);
            if (props != null && props.get() != null) {
                // Create message property
                if(message.getSenderName() == null || message.getSenderName().isEmpty())
                {
                    message.setSenderName("UnKnown");
                    User sender = getUser(message.getSenderId());
                    if(sender == null) {
                        try {
                            sender = UserFactory.findById(message.getSenderId());
                        } catch (SQLException e) {
                            serrr("Cannot Serch In DataBase For User : "+message.getSenderId()+" Error : "+e.getMessage());
                        }
                    }
                    if(sender == null){
                        Task<Response> getUserFromServerTask = new Task<Response>() {
                            @Override
                            protected Response call() throws Exception {
                                return ChatClient.getInstance().getUserById(message.getSenderId().intValue());
                            }
                        };
                        getUserFromServerTask.setOnSucceeded(abc -> {
                            if(getUserFromServerTask.getValue().isSuccess()){
                                if(users.get(message.getSenderId()) != null)
                                    message.setSenderName(users.get(message.getSenderId()).get().getFirstName());
                                //TODO: get this user from server then add his name to this message
                            }
                            else {
                                soutt("Cannot Fetch The Sender Name Of Message : "+ message.toString());
                            }
                        });
                        Executor.submit(getUserFromServerTask);
                    }
                }
                ObjectProperty<Message> messageProp = new SimpleObjectProperty<>(message);
                props.get().messages.put(message.getId(), messageProp);

                // Create controller asynchronously
                Task<SendMessageItemController> task = createSendMessageItemController(message);
                task.setOnSucceeded(e -> {
                    if(task.getValue() != null){
                        if(newMessageReceivedListener != null)
                            newMessageReceivedListener.onNewMessageReceived(message);
                    }
                    props.get().sentMessageControllers.put(
                            message.getId(),
                            new SimpleObjectProperty<>(task.getValue())
                    );
                });
                Executor.submit(task);
                soutt("Added message to chat: " + chatId);
            } else {
                String errorMsg = "Chat not found: " + chatId;
                DiagnosticLogger.log(LOG_FILE, errorMsg);
                serrr(errorMsg + " | Message: " + message.toString());
            }
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            DiagnosticLogger.logOperationDetails(
                    LOG_FILE,
                    operation,
                    details,
                    startTime
            );
            recordPerformance(operation, duration);
        }
    }
    private OnNewMessageListener newMessageReceivedListener;

    public ObjectProperty<SendMessageItemController> getSendMessageItemControllerOf(Message message){
        if(message == null || !chats.containsKey(message.getChatId()))
            return null;
        else if (chatProperties.get(message.getChatId()).get().sentMessageControllers.containsKey(message.getId())){
            return chatProperties.get(message.getChatId()).get().sentMessageControllers.get(message.getId());
        }

        return null;
    }
    public MapProperty<Long,ObjectProperty<SendMessageItemController>> getSendMessageItemControllers(Long chat_id){
        if(chats.containsKey(chat_id)){
            if(chatProperties.get(chat_id).get().sentMessageControllers.size() != getMessages(chat_id).size()){
                //TODO REMOVE Or ADD The Missed Controllers
            }
            return chatProperties.get(chat_id).get().sentMessageControllers;
        }
        return new SimpleMapProperty<>();
    }
    public boolean removeMessageFromChat(long chatId, long messageId) {
        long startTime = System.currentTimeMillis();
        String operation = "Remove Message from Chat";
        String details = "Chat ID: " + chatId + ", Message ID: " + messageId;

        try {
            ObjectProperty<ChatProperties> props = chatProperties.get(chatId);
            if (props != null && props.get() != null) {
                // Remove message and controller
                props.get().messages.remove(messageId);
                props.get().sentMessageControllers.remove(messageId);
                soutt("Removed message from chat: " + chatId);
                return true;
            }
            serrr("Chat not found for message removal: " + chatId);
            return false;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            DiagnosticLogger.logOperationDetails(
                    LOG_FILE,
                    operation,
                    details,
                    startTime
            );
            recordPerformance(operation, duration);
        }
    }

    // User management
    public void addOrUpdateUser(User user) {
        long startTime = System.currentTimeMillis();
        String operation = "Add/Update User";
        String details = "User ID: " + user.getId() + ", Name: " + user.getUsername();

        try {
            Long userId = user.getId();

            if (users.containsKey(userId)) {
                // Update existing user
                users.get(userId).set(user);
                soutt("Updated existing user: " + userId);
            } else {
                // Add new user
                ObjectProperty<User> userProp = new SimpleObjectProperty<>(user);
                users.put(userId, userProp);
                soutt("Added new user: " + userId);
            }
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            DiagnosticLogger.logOperationDetails(
                    LOG_FILE,
                    operation,
                    details,
                    startTime
            );
            recordPerformance(operation, duration);
        }
    }

    public void addUsers(List<User> userList) {
        long startTime = System.currentTimeMillis();
        String operation = "Add Multiple Users";
        String details = "Users count: " + userList.size();

        try {
            for (User user : userList) {
                addOrUpdateUser(user);
            }
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            DiagnosticLogger.logOperationDetails(
                    LOG_FILE,
                    operation,
                    details,
                    startTime
            );
            recordPerformance(operation, duration);
        }
    }

    public ObjectProperty<User> getUserProperty(long userId) {
        return users.get(userId);
    }

    public User getUser(long userId) {
        ObjectProperty<User> userProp = users.get(userId);
        return userProp != null ? userProp.get() : null;
    }

    public boolean removeUser(long userId) {
        long startTime = System.currentTimeMillis();
        String operation = "Remove User";
        String details = "User ID: " + userId;

        try {
            if (users.containsKey(userId)) {
                users.remove(userId);
                soutt("Removed user: " + userId);
                return true;
            }
            serrr("User not found for removal: " + userId);
            return false;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            DiagnosticLogger.logOperationDetails(
                    LOG_FILE,
                    operation,
                    details,
                    startTime
            );
            recordPerformance(operation, duration);
        }
    }

    // Chat participant management
    public void addOrUpdateChatParticipant(ChatParticipant participant) {
        long startTime = System.currentTimeMillis();
        String operation = "Add/Update Chat Participant";
        String details = "Participant ID: " + participant.getId() +
                ", Chat ID: " + participant.getChatId() +
                ", User ID: " + participant.getUserId();

        try {
            long chatId = participant.getChatId();
            ObjectProperty<ChatProperties> props = chatProperties.get(chatId);

            if (props != null && props.get() != null) {
                // Create participant property
                ObjectProperty<ChatParticipant> participantProp =
                        new SimpleObjectProperty<>(participant);

                // Add or update participant
                props.get().participants.put(participant.getId(), participantProp);

                soutt("Added/updated participant in chat: " + chatId);
            } else {
                String errorMsg = "Chat not found for participant: " + chatId;
                DiagnosticLogger.log(LOG_FILE, errorMsg);
                serrr(errorMsg + " | Participant: " + participant.toString());
            }
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            DiagnosticLogger.logOperationDetails(
                    LOG_FILE,
                    operation,
                    details,
                    startTime
            );
            recordPerformance(operation, duration);
        }
    }

    public ObjectProperty<ChatParticipant> getChatParticipantProperty(long chatId, long participantId) {
        ObjectProperty<ChatProperties> props = chatProperties.get(chatId);
        if (props != null && props.get() != null) {
            return props.get().participants.get(participantId);
        }
        return null;
    }

    public ChatParticipant getChatParticipant(long chatId, long participantId) {
        ObjectProperty<ChatParticipant> participantProp = getChatParticipantProperty(chatId, participantId);
        return participantProp != null ? participantProp.get() : null;
    }

    public static Task<Controller> createMediaItemControllerTask(Media media){
        return new Task<Controller>() {
            @Override
            protected Controller call() throws Exception {
//                switch (media.getMediaType()){
//
//                };
                return null;
            }
        };
    }

    public boolean removeChatParticipant(long chatId, long participantId) {
        long startTime = System.currentTimeMillis();
        String operation = "Remove Chat Participant";
        String details = "Chat ID: " + chatId + ", Participant ID: " + participantId;

        try {
            ObjectProperty<ChatProperties> props = chatProperties.get(chatId);
            if (props != null && props.get() != null) {
                props.get().participants.remove(participantId);
                soutt("Removed participant from chat: " + chatId);
                return true;
            }
            serrr("Chat not found for participant removal: " + chatId);
            return false;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            DiagnosticLogger.logOperationDetails(
                    LOG_FILE,
                    operation,
                    details,
                    startTime
            );
            recordPerformance(operation, duration);
        }
    }

    // Media management
    public void addMedia(Media media) {
        mediaMap.put(media.getId(), media);
    }

    public Media getMedia(long mediaId) {
        return mediaMap.get(mediaId);
    }

    public boolean removeMedia(long mediaId) {
        if (mediaMap.containsKey(mediaId)) {
            mediaMap.remove(mediaId);
            return true;
        }
        return false;
    }

    // Utility methods
    private static void soutt(String msg) {
        System.out.println("----- ["+Thread.currentThread().getName()+"][DataModel] : " + msg);
    }

    private static void serrr(String msg) {
        System.err.println("----- ["+Thread.currentThread().getName()+"][DataModel] : " + msg);
    }

    // Shutdown hook for performance reporting
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (instance != null) {
                instance.generatePerformanceReport();
                DiagnosticLogger.log(LOG_FILE, "DataModel shutdown completed");
            }
        }));
    }
}

