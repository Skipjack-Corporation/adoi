package support.skipjack.adoi.converter;

import org.matrix.androidsdk.core.EventDisplay;
import org.matrix.androidsdk.core.JsonUtils;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.rest.model.Event;

import java.util.ArrayList;
import java.util.List;

import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.model.EventEntry;

public class EventConverter extends BaseConverter<Event, EventEntry> {
    @Override
    public EventEntry convert(Event event) {
//        RoomState roomState = MatrixService.get().getRoomState(event.roomId);
        EventEntry eventEntry = new EventEntry();
        eventEntry.roomId = event.roomId;
        eventEntry.eventId = event.eventId;
//        eventEntry.senderName = roomState.getMemberName(event.sender);
//        eventEntry.avatarUrl = MatrixService.get().getUserAvatarUrl(event.sender);
//        eventEntry.eventType = event.getType();
//        eventEntry.timeStamp = event.originServerTs;
//        eventEntry.msgType = JsonUtils.toMessage(event.getContent()).msgtype;
//
//
//        EventDisplay eventDisplay = new EventDisplay(MatrixService.get().getContext());
//        eventDisplay.setPrependMessagesWithAuthor(false);
//        eventEntry.content = String.valueOf(eventDisplay.getTextualDisplay(event,roomState));
        return eventEntry;
    }

    @Override
    public List<EventEntry> convert(List<Event> events) {
        List<EventEntry> messageEntries = new ArrayList<>();
        for (Event event: events){
            messageEntries.add(convert(event));

        }
        return messageEntries;
    }

    @Override
    public Event revert(EventEntry eventEntry) {
        Event event = mxDataHandler.getStore(eventEntry.roomId)
                .getEvent(eventEntry.eventId,eventEntry.roomId);
        return event;
    }

    @Override
    public List<Event> revert(List<EventEntry> eventEntries) {
        List<Event> eventList = new ArrayList<>();
        for (EventEntry entry: eventEntries){
            eventList.add(revert(entry));
        }
        return eventList;
    }
}
