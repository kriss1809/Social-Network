package org.example.service;

import org.example.domain.*;
import org.example.domain.validators.ValidationException;
import org.example.domain.validators.Validator;
import org.example.repository.DataBaseRepository.*;
import org.example.utils.events.ChangeEvent;
import org.example.utils.events.ChangeEventType;
import org.example.utils.events.FriendshipStatusType;
import org.example.utils.observer.Observable;
import org.example.utils.observer.Observer;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Service implements Observable<ChangeEvent>{

    protected Validator validator;
    private static UtilizatorRepoDB userRepo;
    private static MessageRepoDB messageRepo;
    private static PrietenieRepoDB friendshipRepo;
    private static InvitationRepoDB invitationRepo;

    static Map<Long,Integer> viz=new HashMap<Long,Integer>();
    static int maxlen;
    static String users="";

    public Service(Validator val, UtilizatorRepoDB userRepo, PrietenieRepoDB friendshipRepository, MessageRepoDB msgrepo, InvitationRepoDB invRepo) {
        this.userRepo = userRepo;
        this.friendshipRepo=friendshipRepository;
        this.messageRepo = msgrepo;
        this.invitationRepo = invRepo;
        this.validator = val;
    }

    public Iterable <Utilizator> getAllUsers() {
        return userRepo.findAll();
    }

    public Page<Utilizator> getAllUsersPage(Pageable p)
    {
        return userRepo.findAllPage(p);
    }

    public Iterable getAllFriendships() {

        return friendshipRepo.findAll();
    }

    public Page<Prietenie> getAllFriendshipsPage(Pageable p)
    {
        return friendshipRepo.findAllPage(p);
    }

    public Iterable <Message> getAllMessages() {
        return messageRepo.findAll();
    }

    public Page<Message> getAllMessagesPage(Pageable p)
    {
        return messageRepo.findAllPage(p);
    }

    public Iterable<Invitatie> getAllInvitations(){return invitationRepo.findAll();}

    public Page<Invitatie> getAllInvitationsPage(Pageable p)
    {
        return invitationRepo.findAllPage(p);
    }

    public void adaugare_utilizator(Long ID, String prenume, String nume, String username)
    {
        // verificam daca ID-ul exista deja
        Optional<Utilizator> u2 = userRepo.findOne(ID);
        u2.ifPresentOrElse(
                u -> {
                    throw new ValidationException("Exista deja un utilizator cu acest ID!");
                },
                () -> {
                    Utilizator u = new Utilizator(prenume, nume, username);
                    u.setId(ID);
                    userRepo.save(u);
                    notify(new ChangeEvent(ChangeEventType.ADD, u));
                }
        );
    }

    public void modificare_utilizator(Long id, String FirstName, String LastName)
    {
        Optional<Utilizator> existent = userRepo.findOne(id);
        if(existent.isEmpty()){
            throw new ValidationException("Nu exista un utilizator cu acest ID!");
        }
        else {
            Utilizator newUser= new Utilizator(FirstName,LastName, existent.get().getUsername());
            newUser.setId(id);
            userRepo.update(newUser);
            notify(new ChangeEvent(ChangeEventType.UPDATE, newUser));
        }
    }

    public void stergere_utilizator(Long ID)
    {
        Optional<Utilizator> u = userRepo.findOne(ID);
        u.ifPresentOrElse(
                userToDelete -> {
                    userRepo.delete(ID);
                    notify(new ChangeEvent(ChangeEventType.DELETE, userToDelete));
                },
                () -> {
                    throw new ValidationException("Utilizatorul nu exista");
                }
        );
    }

    public Optional<Utilizator> cautare_utilizator(Long id)
    {
        Optional<Utilizator> u = userRepo.findOne(id);
        if(u.isPresent())
            return u;
        else
            throw new ValidationException("Utilizatorul nu exista");
    }

    public Optional<Utilizator> cautare_utilizator_username(String username)
    {
        Optional<Utilizator> u = userRepo.findOneUsername(username);
        if(u.isPresent())
            return u;
        else
            throw new ValidationException("Utilizatorul nu exista");
    }
    public void adaugare_prieten(Long ID, Long fID)
    {
        Optional<Utilizator> userOptional1 = userRepo.findOne(ID);
        Optional<Utilizator> userOptional2 = userRepo.findOne(fID);

        Optional<Prietenie> p1 = friendshipRepo.findOne(new Tuple<>(ID, fID));
        Optional<Prietenie> p2 = friendshipRepo.findOne(new Tuple<>(fID, ID));

        if(p1.isPresent() || p2.isPresent())
            throw new ValidationException("Aceasta prietenie exista deja!");

        else {

            userOptional1.ifPresentOrElse(
                    user1 -> userOptional2.ifPresentOrElse(
                            user2 -> {
                                Prietenie friendship = new Prietenie(user1, user2, LocalDateTime.now());
                                friendshipRepo.save(friendship);
                                notify(new ChangeEvent(ChangeEventType.ADD, friendship));
                            },
                            () -> {
                                throw new ValidationException("Utilizatorul cu ID " + fID + " nu exista");
                            }
                    ),
                    () -> {
                        throw new ValidationException("Utilizatorul cu ID " + ID + " nu exista");
                    }
            );
        }
    }


    public void stergere_prieten(Long ID, Long fID)
    {
        Optional<Utilizator> userOptional1 = userRepo.findOne(ID);
        Optional<Utilizator> userOptional2 = userRepo.findOne(fID);

        userOptional1.ifPresentOrElse(
                user1 -> userOptional2.ifPresentOrElse(
                        user2 -> {
                            Tuple<Long, Long> tuple = new Tuple<>(ID, fID);
                            Optional<Prietenie> p = friendshipRepo.findOne(tuple);
                            if(p.isPresent()) {
                                friendshipRepo.delete(tuple);
                                p.ifPresent(prietenie -> notify(new ChangeEvent(ChangeEventType.DELETE, null, prietenie)));
                            }
                        },
                        () -> {
                            throw new ValidationException("Utilizatorul cu ID " + fID + " nu exista");
                        }
                ),
                () -> {
                    throw new ValidationException("Utilizatorul cu ID " + ID + " nu exista");
                }
        );
    }


    public static void DFS(Long x) {
        viz.put(x, 1);
        Optional<Utilizator> u = userRepo.findOne(x);
        if (u.isPresent()) {
            u.get().getFriends().forEach(p -> {
                if (viz.get(p.getId()) != 1) {
                    DFS(p.getId());
                }
            });
        }
    }

    public static int nr_comunitati()
    {
        viz.clear();

        userRepo.findAll().forEach(u -> viz.put(((Utilizator) u).getId(), 0));

        return (int) StreamSupport.stream(Spliterators.spliteratorUnknownSize(userRepo.findAll().iterator(), 0), false)
                .filter(u -> viz.get(((Utilizator) u).getId()) == 0)
                .peek(u -> DFS(((Utilizator) u).getId()))
                .count();
    }

    public static void DFS_updated(Long x, int[] len, StringBuilder names) {
        viz.put(x, 1);
        Optional<Utilizator> userOptional = userRepo.findOne(x);

        if (userOptional.isPresent()) {
            Utilizator user = userOptional.get();
            len[0] = len[0] + 1;

            names.append(user.getFirstName()).append(" ").append(user.getLastName()).append(",");

            if (len[0] > maxlen) {
                maxlen = len[0];
                users = names.toString();
            }

            user.getFriends().forEach(prieten -> {
                if (viz.get(prieten.getId()) != 1) {
                    DFS_updated(prieten.getId(), len, names);
                }
            });
        }
    }

    public static String comunitate_sociabila() {
        String mostSociableCommunity = "";
        int maxLen = 0;

        Iterable<Utilizator> lista_utilizatori = userRepo.findAll();
        for (Utilizator user : lista_utilizatori) {
            viz.clear();
            userRepo.findAll().forEach(u -> viz.put(((Utilizator) u).getId(), 0));
            int[] len = new int[]{0};
            StringBuilder names = new StringBuilder();
            DFS_updated(((Utilizator) user).getId(), len, names);

            if (len[0] > maxLen) {
                maxLen = len[0];
                mostSociableCommunity = names.toString();
            }
        }
        return mostSociableCommunity;
    }

    public static List<Utilizator> listaNprieteni(Long N) {
        Iterable<Utilizator> utilizatori = userRepo.findAll();

        return StreamSupport.stream(utilizatori.spliterator(), false)
                .filter(u -> u.getFriends().size() >= N)
                .collect(Collectors.toList());
    }

    public static List<PrietenDTO> prietenii_utilizator_dat(Long ID, Long luna)
    {
        Optional <Utilizator> user = userRepo.findOne(ID);

        if(user.isPresent()) {
            Iterable<Prietenie> friendships = friendshipRepo.findAll();
            return StreamSupport.stream(friendships.spliterator(), false).
                    filter(friend -> {
                        LocalDateTime time = friend.getDate();
                        return (time.getMonthValue() == luna);
                    }).
                    map(friendship -> {

                        if (Objects.equals(friendship.getId().getLeft(), user.get().getId())) {
                            return new PrietenDTO((Utilizator) (userRepo.findOne(friendship.getId().getRight())).get(),
                                    friendship.getDate());
                        } else if (Objects.equals(friendship.getId().getRight(), user.get().getId())) {
                            return new PrietenDTO((Utilizator) (userRepo.findOne(friendship.getId().getLeft())).get(),
                                    friendship.getDate());
                        }
                        return null;
                    }).map(o -> (PrietenDTO) o).collect(Collectors.toList());
        }
        else
            return Collections.emptyList();
    }

    public static Iterable<Utilizator> utilizatori_string(String text)
    {
        return userRepo.utilizatori_string(text);
    }

    public void adaugare_mesaj(Long id, Long id1, Long id2, String text, LocalDateTime data, Long raspuns)
    {
        Optional<Message> m2 = messageRepo.find(id);
        m2.ifPresentOrElse(
                msg -> {
                    throw new ValidationException("Exista deja un mesaj cu acest ID!");
                },
                () -> {
                    Message msg = new Message(id1, id2, text, data);
                    msg.setId(id);
                    msg.setReply(raspuns);
                    messageRepo.save(msg);
                    notify(new ChangeEvent(ChangeEventType.ADD, msg));
                }
        );
    }

    public static String afisare_mesaje(Long id_user1, Long id_user2)
    {
        Iterable<Message> msgs =  messageRepo.conversatii_utilizatori(id_user1, id_user2);
        StringBuilder result = new StringBuilder();

        for (Message message : msgs) {
            result.append(message.toString()); // You might need to adjust this depending on your Message class
            result.append(System.lineSeparator()); // Add a new line after each message
        }

        return result.toString();
    }


    public void adaugare_invitatie(Long ID, Long fID, FriendshipStatusType status)
    {
        Optional<Utilizator> userOptional1 = userRepo.findOne(ID);
        Optional<Utilizator> userOptional2 = userRepo.findOne(fID);

        Optional<Invitatie> inv = invitationRepo.findOne(new Tuple<>(ID, fID));

        if(inv.isPresent())
            throw new ValidationException("Aceasta invitatie exista deja!");
        else {

            userOptional1.ifPresentOrElse(
                    user1 -> userOptional2.ifPresentOrElse(
                            user2 -> {
                                Invitatie i = new Invitatie(ID, fID, status);
                                invitationRepo.save(i);
                                notify(new ChangeEvent(ChangeEventType.ADD, i));
                                if(status==FriendshipStatusType.accepted)
                                    adaugare_prieten(ID, fID);
                            },
                            () -> {
                                throw new ValidationException("Utilizatorul cu ID " + fID + " nu exista");
                            }
                    ),
                    () -> {
                        throw new ValidationException("Utilizatorul cu ID " + ID + " nu exista");
                    }
            );
        }
    }

    public void modificare_invitatie(Long id1, Long id2, FriendshipStatusType status)
    {
        Optional<Utilizator> userOptional1 = userRepo.findOne(id1);
        Optional<Utilizator> userOptional2 = userRepo.findOne(id2);

        Optional<Invitatie> inv = invitationRepo.findOne(new Tuple<>(id1, id2));

        if(!inv.isPresent())
            throw new ValidationException("Aceasta invitatie nu exista!");
        else {

            userOptional1.ifPresentOrElse(
                    user1 -> userOptional2.ifPresentOrElse(
                            user2 -> {
                                Invitatie i = new Invitatie(id1, id2, status);
                               invitationRepo.update(i);
                                notify(new ChangeEvent(ChangeEventType.UPDATE, i));
                                if(status==FriendshipStatusType.accepted)
                                    adaugare_prieten(id1, id2);
                            },
                            () -> {
                                throw new ValidationException("Utilizatorul cu ID " + id1 + " nu exista");
                            }
                    ),
                    () -> {
                        throw new ValidationException("Utilizatorul cu ID " + id2 + " nu exista");
                    }
            );
        }
    }

    // functii noi pt observer

    private List<Observer<ChangeEvent>> observers = new ArrayList<>();
    @Override
    public void addObserver(Observer<ChangeEvent> e) {
        observers.add(e);
    }
    @Override
    public void removeObserver(Observer<ChangeEvent> e) {
        observers.remove(e);
    }
    @Override
    public void notify(ChangeEvent t) {
        observers.stream().forEach(x->x.update(t));
    }

}
