package com.github.Emcc13.TicketsProxy.Database;

import com.github.Emcc13.TicketsProxy.Config.ConfigManager;
import com.github.Emcc13.TicketsProxy.ProxyTickets;
import com.github.Emcc13.TicketsProxy.Util.Tuple;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBTicket {
    private enum PH {
        ID("%ID%"),
        PLAYER("%PLAYER%"),
        SERVER("%SERVER%"),
        WORLD("%WORLD%"),
        X("%X%"),
        Y("%Y%"),
        Z("%Z%"),
        RESPONDER("%RESPONDER%"),
        CLAIMER("%CLAIMER%"),
        REQUEST("%REQUEST%"),
        ANSWER("%ANSWER%"),
        TICKETTYPE("%TICKETTYPE%"),

        DATE_YYYY("YYYY%"),
        DATE_YY("YY%"),
        DATE_MM("MM%"),
        DATE_DD("DD%"),
        DATE_hh("hh%"),
        DATE_mm("mm%"),
        DATE_ss("ss%"),
        ;

        private final String name;

        private PH(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }


    private Integer id;

    private String enquirer; // UUID
    private String ticketType; // Console = 0, Player = 1, Player but hidden = 2
    private String request;
    private Date requestDate;

    private String server;
    private String world;
    private Double x_pos;
    private Double y_pos;
    private Double z_pos;
    private Float azimuth;
    private Float elevation;

    private String claimedBy;
    private Date claimingDate;

    private String responder;
    private String answer;
    private Date answerDate;

    private Date readDate;


    public DBTicket(Integer id, String enquirer, String ticketType, String request, Date requestDate,
                    String server, String world, double x_pos, double y_pos, double z_pos, float azimuth, float elevation) {
        this.id = id;
        this.enquirer = enquirer;
        this.ticketType = ticketType;
        this.request = request;
        this.requestDate = requestDate;
        this.server = server;
        this.world = world;
        this.x_pos = x_pos;
        this.y_pos = y_pos;
        this.z_pos = z_pos;
        this.azimuth = azimuth;
        this.elevation = elevation;
    }

    public DBTicket(Integer id, String enquirer, String ticketType, String request, Date requestDate,
                    String server, String world, double x_pos, double y_pos, double z_pos, float azimuth, float elevation,
                    String claimedBy, Date claimingDate) {
        this(id, enquirer, ticketType, request, requestDate, server, world, x_pos, y_pos, z_pos, azimuth, elevation);
        this.claimedBy = claimedBy;
        this.claimingDate = claimingDate;
    }

    public DBTicket(Integer id, String enquirer, String ticketType, String request, Date requestDate,
                    String server, String world, double x_pos, double y_pos, double z_pos, float azimuth, float elevation,
                    String claimedBy, Date claimingDate,
                    String responder, String answer, Date answerDate) {
        this(id, enquirer, ticketType, request, requestDate, server, world, x_pos, y_pos, z_pos, azimuth, elevation,
                claimedBy, claimingDate);
        this.responder = responder;
        this.answer = answer;
        this.answerDate = answerDate;
    }

    public DBTicket(Integer id, String enquirer, String ticketType, String request, Date requestDate,
                    String server, String world, double x_pos, double y_pos, double z_pos, float azimuth, float elevation,
                    String claimedBy, Date claimingDate,
                    String responder, String answer, Date answerDate,
                    Date readDate) {
        this(id, enquirer, ticketType, request, requestDate, server, world, x_pos, y_pos, z_pos, azimuth, elevation,
                claimedBy, claimingDate, responder, answer, answerDate);
        this.readDate = readDate;
    }

    public Integer getId() {
        return id;
    }

    public String getEnquirer() {
        return enquirer;
    }

    public String getTicketType() {
        return ticketType;
    }

    public String getRequest() {
        return request;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public String getServer() {
        return server;
    }

    public String getWorld() {
        return world;
    }

    public Double getX_pos() {
        return x_pos;
    }

    public String formatedX() {
        String result = ((String) ProxyTickets.getInstance().getCachedConfig().get(ConfigManager.COORDINATE_FORMAT_KEY));
        return String.format(result, this.x_pos);
    }

    public String formatedY() {
        String result = ((String) ProxyTickets.getInstance().getCachedConfig().get(ConfigManager.COORDINATE_FORMAT_KEY));
        return String.format(result, this.y_pos);
    }

    public String formatedZ() {
        String result = ((String) ProxyTickets.getInstance().getCachedConfig().get(ConfigManager.COORDINATE_FORMAT_KEY));
        return String.format(result, this.z_pos);
    }

    public Double getY_pos() {
        return y_pos;
    }

    public Double getZ_pos() {
        return z_pos;
    }

    public Float getAzimuth() {
        return azimuth;
    }

    public Float getElevation() {
        return elevation;
    }

    public String getClaimedBy() {
        return claimedBy;
    }

    public Date getClaimingDate() {
        return claimingDate;
    }

    public String getResponder() {
        return responder;
    }

    public String getAnswer() {
        return answer;
    }

    public Date getAnswerDate() {
        return answerDate;
    }

    public Date getReadDate() {
        return readDate;
    }

    public TextComponent format(List<TextComponent> rawMessage, Map<String, String> ticketTypes) {
        TextComponent result = new TextComponent();
        for (TextComponent tc : rawMessage) {
            String tcText = replacePlaceholder(tc.getText(), ticketTypes);
            List<Tuple<Integer, Integer>> indeces = extractUrls(tcText);
            TextComponent copy;
            int last_index = 0;
            for (Tuple<Integer, Integer> index : indeces) {
                String url = tcText.substring(index.first, index.second);
                copy = buildComponent(tc, tcText.substring(last_index, index.first), ticketTypes);
                result.addExtra(copy);
                copy = new TextComponent(url);
                if (!(url.startsWith("https") || url.startsWith("http"))) {
                    url = "https://" + url;
                }
                copy.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                copy.setUnderlined(true);
                result.addExtra(copy);
                last_index = index.second;
            }
            copy = buildComponent(tc, tcText.substring(last_index), ticketTypes);
            result.addExtra(copy);
        }
        return result;
    }

    private TextComponent buildComponent(TextComponent original, String text, Map<String, String> ticketTypes) {
        TextComponent copy = new TextComponent(text);
        HoverEvent hover = original.getHoverEvent();
        if (hover != null) {
            List<Content> contents = new LinkedList<>();
            for (Content content : hover.getContents()) {
                contents.add(new Text(replacePlaceholder(
                        ((String) ((Text) content).getValue()),
                        ticketTypes
                )));
            }
            copy.setHoverEvent(new HoverEvent(hover.getAction(), contents));
        }
        ClickEvent click = original.getClickEvent();
        if (click != null) {
            copy.setClickEvent(new ClickEvent(click.getAction(),
                    replacePlaceholder(click.getValue(), ticketTypes)));
        }
        return copy;
    }

    private String replacePlaceholder(String template, Map<String, String> ticketTypes) {
        String result = template
                .replace(PH.ID.name, String.format("%d", this.id))
                .replace(PH.PLAYER.name, this.enquirer)
                .replace(PH.SERVER.name, this.server)
                .replace(PH.WORLD.name, this.world)
                .replace(PH.X.name, String.valueOf(this.formatedX()))
                .replace(PH.Y.name, String.valueOf(this.formatedY()))
                .replace(PH.Z.name, String.valueOf(this.formatedZ()))
                .replace(PH.RESPONDER.name, ((this.responder != null) ? this.responder : ""))
                .replace(PH.REQUEST.name, String.valueOf(this.request))
                .replace(PH.ANSWER.name, this.answer != null ? this.answer : "")
                .replace(PH.TICKETTYPE.name, ticketTypes.getOrDefault(this.ticketType, ""))
                .replace(PH.CLAIMER.name, this.claimedBy != null ? this.claimedBy : "");
        result = replaceDateTimePlaceholder(result, "create");
        result = replaceDateTimePlaceholder(result, "claim");
        result = replaceDateTimePlaceholder(result, "close");
        result = replaceDateTimePlaceholder(result, "read");
        return result;
    }

    private String replaceDateTimePlaceholder(String template, String prefix) {
        ZonedDateTime zdt = null;
        switch (prefix) {
            case "claim":
                if (this.getClaimingDate() != null)
                    zdt = this.getClaimingDate().toInstant().atZone(ZoneId.systemDefault());
                break;
            case "close":
                if (this.getAnswerDate() != null)
                    zdt = this.getAnswerDate().toInstant().atZone(ZoneId.systemDefault());
                break;
            case "read":
                if (this.getReadDate() != null)
                    zdt = this.getReadDate().toInstant().atZone(ZoneId.systemDefault());
                break;
            default:
                if (this.getRequestDate() != null)
                    zdt = this.getRequestDate().toInstant().atZone(ZoneId.systemDefault());
                break;
        }
        String result = template;
        if (zdt != null)
            result = result
                    .replace("%" + prefix + "_" + PH.DATE_YYYY.name, String.format("%04d", zdt.getYear()))
                    .replace("%" + prefix + "_" + PH.DATE_YY.name, String.format("%02d", zdt.getYear() % 100))
                    .replace("%" + prefix + "_" + PH.DATE_MM.name, String.format("%02d", zdt.getMonthValue()))
                    .replace("%" + prefix + "_" + PH.DATE_DD.name, String.format("%02d", zdt.getDayOfMonth()))
                    .replace("%" + prefix + "_" + PH.DATE_hh.name, String.format("%02d", zdt.getHour()))
                    .replace("%" + prefix + "_" + PH.DATE_mm.name, String.format("%02d", zdt.getMinute()))
                    .replace("%" + prefix + "_" + PH.DATE_ss.name, String.format("%02d", zdt.getSecond()));
        return result;
    }

    public static List<Tuple<Integer, Integer>> extractUrls(String text) {
        List<Tuple<Integer, Integer>> containedUrls = new ArrayList<Tuple<Integer, Integer>>();
        Pattern pattern = Pattern.compile((String) ProxyTickets.getInstance().getCachedConfig().get(ConfigManager.TICKET_FORMAT_LINK_KEY));
        Matcher urlMatcher = pattern.matcher(text);
        while (urlMatcher.find()) {
            containedUrls.add(new Tuple<>(urlMatcher.start(0), urlMatcher.end(0)));
        }
        return containedUrls;
    }
}
