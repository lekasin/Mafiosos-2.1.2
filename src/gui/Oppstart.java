package gui;

import Utils.SkjermUtil;
import Utils.InnstillingsUtil;
import Utils.VeiledningsUtil;
import Utils.MenyUtil;
import Utils.TvUtil;
import datastruktur.Spillerliste;
import personer.Rolle;
import personer.Spiller;
import personer.roller.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Oppstart implements ActionListener {

    JPanel innhold;
    JTextField navnefelt;
    JLabel tekst, spesialistTekst = new JLabel();
    Vindu vindu;
    private static Spillerliste spillere;

    public static Rolle[] roller;
    ArrayList<String> gjenstander;
    Knapp fortsett;

    public int antallspillere = 0;
    int fase = 100, mafiaer = -1, politi = -1, venner = -1, backup, tid = 6;
    private static int indeks = 0, personIndeks = -1;
    boolean fordelerSpesialister, fjerning;

    public static final int VELGSPILLERE = 100, VELGROLLER = 101, VELGSPESIALISTER = 102, VELGGJENSTANDER = -1, HVEMERHVA = 103, STARTSPILL = 104;
    public static final int TITTEL = 50, UNDERTITTEL = 30;

    private Mafia mafia;

    public Oppstart(Vindu vindu) {
        this.vindu = vindu;
        spillere = vindu.spillere;
        this.innhold = vindu.innhold;

        vindu.setOppstart(this);
        vindu.kontroll(new Lytter(), -1);

        indeks = 0;
        roller = null;
        velgSpillere();
        InnstillingsUtil.setOppstart(this);
    }

    public Spillerliste getSpillere() {
        return spillere;
    }

    public void nyfase(int f) {
        switch (f) {
            case VELGSPILLERE:
                velgSpillere();
                break;
            case VELGROLLER:
                velgRoller();
                break;
            case VELGSPESIALISTER:
                velgSpesialister();
                break;
            case VELGGJENSTANDER:
                velgGjenstander();
                break;
            case HVEMERHVA:
                hvemErHva();
                informer("Følgende roller er med:");
                visRoller(spillere.visRoller(roller));
                SkjermUtil.fullLogg(spillere.visRoller(roller));
                break;
            case STARTSPILL:
                startSpill();
                break;
        }
        MenyUtil.visSpillMeny(vindu, f);
    }

    private void velgSpillere() {
        fase = VELGSPILLERE;
        vindu.kontroll.setVisible(false);
        vindu.visLogg(false);
        VeiledningsUtil.setTekst("Spillerregistrering:\n" +
                "Skriv inn navnet på nye spillere og trykk enter, eller klikk på registrer for å registrere dem.\n" +
                "For å fjerne en spiller, trykker du på spillerens navn i listen og trykker fjern.\n" +
                "Trykk fortsett for å gå til neste steg.");
        Lytter lytt = new Lytter();
        navnefelt = new JTextField();
        navnefelt.setPreferredSize(Knapp.HEL);
        navnefelt.addActionListener(this);
        navnefelt.requestFocusInWindow();

        SkjermUtil.tittuler("Hvem skal spille?");

        Knapp registrer = new Knapp("Registrer", Knapp.HEL, this);
        fortsett = vindu.getFortsett();
        fortsett.setPreferredSize(Knapp.HEL);
        fortsett.setVisible(true);
        Knapp fjern = new Knapp("Fjern", Knapp.HEL, e -> fjernSpillere());

        informer(spillere.toString());

        innhold = vindu.innhold();
        innhold.add(navnefelt);
        innhold.add(registrer);
        innhold.add(fjern);
        innhold.add(fortsett);
        innhold.add(new Knapp("Fjern alle", Knapp.HEL, e -> {
            spillere.spillere().clear();
            vindu.tømListe();
            vindu.velgIngen();
            informer(spillere.toString());
        }));
        innhold.add(new Knapp("Legg til alle", Knapp.HEL, lytt));
    }

    private void leggTilSpiller(Spiller spiller){
        if (spillere.finnSpillerMedNavn(spiller.navn()) != null)
            return;
        spillere.leggTil(spiller);
        antallspillere++;
        vindu.leggTilListe(spiller);
        vindu.setListetittel("Spillere: " + spillere.spillere().size());
        informer(spillere.toString());
    }

    private void fjernSpillere(){
        for (Spiller spiller : vindu.hentValgteSpillere()) {
            vindu.fjernFraListe(spiller);
            spillere.fjern(spiller.navn());
            antallspillere--;
        }
        informer(spillere.toString());
        navnefelt.setText("");
        navnefelt.requestFocusInWindow();
        vindu.setListetittel("Spillere: " + spillere.spillere().size());
        vindu.velgIngen();
    }

    public void velgRoller() {
        fase = VELGROLLER;
        vindu.kontroll.setVisible(true);
        VeiledningsUtil.setTekst("Rollevalg:\n" +
                "Velg hvilke roller som skal være med i spill ved å klikke på rollens navn.\n" +
                "Når du velger en rolle, vil knappen deaktiveres, med mindre rollen kan ha flere spillere.\n" +
                "For å fjerne en rolle trykker du på fjern, og velger rollen som skal fjernes.\n" +
                "For å nullstille rollene, trykker du på tilbake.\n" +
                "Når nok roller er valgt, blir du automatisk tatt videre til neste steg.");

        innhold = vindu.innhold();
        SkjermUtil.tittuler("Hvilke roller skal være med?");
        vindu.kontroll(new Lytter(), VELGROLLER);
        vindu.visLogg(true);

        if (mafia == null)
            mafia = new Mafia();
        Politi politi = new Politi();
        Bestevenn venn = new Bestevenn();
        innhold.add(new Knapp("Mafia", mafia, Knapp.KVART, this));

        innhold.add(new Knapp("Agent Smith", new Smith(), Knapp.KVART, this));
        innhold.add(new Knapp("Aktor", new Aktor(), Knapp.KVART, this));
        innhold.add(new Knapp("Anarkist", new Anarkist(), Knapp.KVART, this));
        innhold.add(new Knapp("Arving", new Arving(), Knapp.KVART, this));
        innhold.add(new Knapp("Astronaut", new Astronaut(), Knapp.KVART, this));

        innhold.add(new Knapp("Barnslige Erik", new Erik(), Knapp.KVART, this));
        innhold.add(new Knapp("Bedrager", new Bedrager(), Knapp.KVART, this));
        innhold.add(new Knapp("Belieber", new Belieber(), Knapp.KVART, this));
        innhold.add(new Knapp("Bestemor", new Bestemor(), Knapp.KVART, this));
        innhold.add(new Knapp("Bestevenn", venn, Knapp.KVART, this));
        innhold.add(new Knapp("Bodyguard", new BodyGuard(), Knapp.KVART, this));
        innhold.add(new Knapp("Bomber", new Bomber(), Knapp.KVART, this));
        innhold.add(new Knapp("Bøddelen", new Boddel(), Knapp.KVART, this));

        innhold.add(new Knapp("CopyCat", new CopyCat(), Knapp.KVART, this));
        innhold.add(new Knapp("Cupid", new Cupid(), Knapp.KVART, this));

        innhold.add(new Knapp("Distré Didrik", new Didrik(), Knapp.KVART, this));
        innhold.add(new Knapp("Drømmer", new Drommer(), Knapp.KVART, this));

        innhold.add(new Knapp("Filosof", new Filosof(), Knapp.KVART, this));

        innhold.add(new Knapp("Gærne Berit", new Berit(), Knapp.KVART, this));

        innhold.add(new Knapp("Hammer", new Hammer(), Knapp.KVART, this));
        innhold.add(new Knapp("Havfrue", new Havfrue(), Knapp.KVART, this));
        innhold.add(new Knapp("Heisenberg", new Heisenberg(), Knapp.KVART, this));
        innhold.add(new Knapp("HMS-Ansvarlig", new HMSansvarlig(), Knapp.KVART, this));

        innhold.add(new Knapp("Illusjonist", new Illusjonist(), Knapp.KVART, this));
        innhold.add(new Knapp("Informant", new Informant(), Knapp.KVART, this));
        innhold.add(new Knapp("Insider", new Insider(politi), Knapp.KVART, this));

        innhold.add(new Knapp("Jesus", new Jesus(), Knapp.KVART, this));
        innhold.add(new Knapp("Joker", new Joker(), Knapp.KVART, this));
        innhold.add(new Knapp("Julenissen", new Julenissen(), Knapp.KVART, this));
        innhold.add(new Knapp("Jørgen", new Jorgen(), Knapp.KVART, this));

        innhold.add(new Knapp("Kirsten Giftekniv", new Kirsten(), Knapp.KVART, this));
        innhold.add(new Knapp("Kløna", new Klona(), Knapp.KVART, this));

        innhold.add(new Knapp("Lege", new Lege(), Knapp.KVART, this));
        innhold.add(new Knapp("Liten jente", new Jente(), Knapp.KVART, this));
        innhold.add(new Knapp("Løgner", new Logner(), Knapp.KVART, this));

        innhold.add(new Knapp("Magnus Carlsen", new Carlsen(), Knapp.KVART, this));
        innhold.add(new Knapp("Marius", new Marius(), Knapp.KVART, this));
        innhold.add(new Knapp("Morder", new Morder(), Knapp.KVART, this));

        innhold.add(new Knapp("Obduksjonist", new Obduksjonist(), Knapp.KVART, this));

        innhold.add(new Knapp("Politi", politi, Knapp.KVART, this));
        innhold.add(new Knapp("Princess98", new Princess(), Knapp.KVART, this));
        innhold.add(new Knapp("Psykolog", new Psykolog(), Knapp.KVART, this));

        innhold.add(new Knapp("Quisling", new Quisling(), Knapp.KVART, this));

        innhold.add(new Knapp("Ravn", new Ravn(), Knapp.KVART, this));
        innhold.add(new Knapp("Rex", new Rex(), Knapp.KVART, this));

        innhold.add(new Knapp("Sherlock", new Sherlock(), Knapp.KVART, this));
        innhold.add(new Knapp("Skytsengel", new Skytsengel(), Knapp.KVART, this));
        innhold.add(new Knapp("Snylter", new Snylter(), Knapp.KVART, this));
        innhold.add(new Knapp("Snåsamannen", new Snasamann(), Knapp.KVART, this));
        innhold.add(new Knapp("Sofasurfer", new Sofasurfer(), Knapp.KVART, this));
        innhold.add(new Knapp("Special Guy", new Specialguy(), Knapp.KVART, this));

        innhold.add(new Knapp("Tjukkas", new Tjukkas(), Knapp.KVART, this));
        innhold.add(new Knapp("Trompet", new Trompet(), Knapp.KVART, this));
        innhold.add(new Knapp("Tyler Durden", new Tyler(), Knapp.KVART, this));
        innhold.add(new Knapp("Tyster", new Tyster(), Knapp.KVART, this));

        innhold.add(new Knapp("Ulf Omar", new UlfOmar(), Knapp.KVART, this));
        innhold.add(new Knapp("Undercover", new Undercover(mafia), Knapp.KVART, this));

        innhold.add(new Knapp("VaraMafia", new Vara(), Knapp.KVART, this));

        innhold.add(new Knapp("Youtuber", new Youtuber(), Knapp.KVART, this));

        innhold.add(new Knapp("Zombie", new Zombie(), Knapp.KVART, this));

        if (roller == null) {
            antallspillere = spillere.length();
            roller = new Rolle[100];
            roller[Rolle.MAFIA] = mafia;
            informer(spillere.rolleString(roller, --antallspillere));
        } else {
            informer(spillere.rolleString(roller, antallspillere));
            for (Component c : innhold.getComponents()) {
                if (c instanceof Knapp)
                    c.setEnabled(false);
            }
        }
    }

    public void velgGjenstander() {
        fase = VELGGJENSTANDER;
        gjenstander = new ArrayList<>();
        innhold = vindu.innhold();
        SkjermUtil.tittuler("Hvilke gjenstander skal være med?");

        innhold.add(new Knapp("Pistol", Knapp.KVART, this));
        innhold.add(new Knapp("Skuddsikker vest", Knapp.KVART, this));
        innhold.add(new Knapp("Benåding", Knapp.KVART, this));
        innhold.add(new Knapp("Trippelstemme", Knapp.KVART, this));
        innhold.add(new Knapp("Felle", Knapp.KVART, this));

        innhold.add(vindu.getTilbake());
        innhold.add(fortsett);

        fortsett.setPreferredSize(Knapp.HALV);


        informer("Gjenstander(" + spillere.spillere().size() + " spillere):");
    }

    public void hvemErHva() {
        fase = HVEMERHVA;
        VeiledningsUtil.setTekst("Rollefordeling:\n" +
                "Her skal rollene fordeles på spillerne ved å vekke rollen som vises til høyre, " +
                "og trykke på navnene til personene som våkner.\n" +
                "Husk at noen roller har flere spillere, og at alle mafiaspesialister er mafia.\n" +
                "Husk også at roller som undercover og insider må våkne sammen med henholdsvis mafia og politi.");

        innhold = vindu.innhold();
        SkjermUtil.tittuler("Hvem er hva?");
        vindu.kontroll(new Lytter(), HVEMERHVA);
        fortsett = vindu.getFortsett();
        fortsett.setVisible(false);

        tekst = new JLabel();
        tekst.setFont(new Font("Arial", Font.BOLD, Oppstart.TITTEL));
        tekst.setHorizontalAlignment(SwingConstants.CENTER);
        tekst.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel p = new JPanel(new BorderLayout());
        p.setVisible(true);
        p.add(tekst, BorderLayout.NORTH);
        p.add(innhold, BorderLayout.CENTER);

        vindu.personknapper(innhold, this);
        vindu.oppdaterRamme(p);

        nesteRolle();
    }

    private void fordelSpesialister() {
        fordelerSpesialister = true;
        String spesialist = mafia.hentLedigSpesialist();
        TvUtil.visMafiaRolleBilde(mafia, spesialist);
        if (spesialist.isEmpty())
            return;
        spesialistTekst = new JLabel();
        spesialistTekst.setPreferredSize(new Dimension(600, 135));
        spesialistTekst.setFont(new Font("Arial", Font.BOLD, Oppstart.UNDERTITTEL));
        spesialistTekst.setHorizontalAlignment(JLabel.CENTER);
        spesialistTekst.setText("Hvem er " + spesialist + "?");
        innhold.add(spesialistTekst);

    }

    public void stopSpesialistFordeling() {
        fordelerSpesialister = false;
        innhold.remove(spesialistTekst);
        innhold.revalidate();
        innhold.repaint();
    }

    public void autoFordelRoller() {
        mafia.nullstillSpesialister();
        spillere.fordelRoller(roller);
        innhold = vindu.innhold();
        innhold.removeAll();
        fortsett.setVisible(true);

        SkjermUtil.tittuler("Hvem er hva?");

        tekst = new JLabel();
        tekst.setFont(new Font("Arial", Font.BOLD, Oppstart.TITTEL));
        tekst.setHorizontalAlignment(SwingConstants.CENTER);
        tekst.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        innhold.add(tekst);
        vindu.oppdaterRamme(innhold);

        personIndeks = -1;
        nestePerson();
    }

    public void velgSpesialister() {
        VeiledningsUtil.setTekst("Spesialistvalg:\n" +
                "Her kan du velge om mafiaen skal noen spesialister på sitt lag, som får en ekstra engangsevne." +
                "For å legge til en spesialist, trykker knappen med spesialistens navn. " +
                "Om du ikke vil ha noen spesialister, eller har valgt alle du vil ha, trykker du på fortsett.\n" +
                "For å nullstille spesialistene, trykker du tilbake én gang.\n" +
                "Trykker du på tilbake igjen, går du tilbake til rollevalg.");
        fase = VELGSPESIALISTER;
        innhold = vindu.innhold();
        mafia.fjernAlleSpesialister();

        SkjermUtil.tittuler("Hvilke spesialister skal mafiaen ha?");

        innhold.add(new Knapp(Mafia.SNIPER, Knapp.KVART, this));
        innhold.add(new Knapp(Mafia.SJÅFØR, Knapp.KVART, this));
        innhold.add(new Knapp(Mafia.SABOTØR, Knapp.KVART, this));
        innhold.add(new Knapp(Mafia.FORFALSKER, Knapp.KVART, this));

        vindu.kontroll(new Lytter(), VELGSPESIALISTER);
    }

    public void startSpill() {
        fase = STARTSPILL;
        innhold = vindu.innhold();
        vindu.kontroll.remove(1);
        fortsett.setVisible(true);
        fortsett.setText("Start spill!");
        fortsett.setPreferredSize(Knapp.SUPER);
        innhold.add(fortsett);
        SkjermUtil.fullLogg("");
        SkjermUtil.tittuler("Klar til å begynne spillet?");
    }

    public void nesteRolle() {
        while (indeks < roller.length - 1 && roller[indeks] == null) indeks++;

        if (indeks == Rolle.BESTEVENN && roller[indeks] != null) {
            if (venner == -1)
                venner = roller[indeks].antall() - 1;
            else if (venner > 0)
                venner--;
            else {
                venner = -1;
                indeks++;
                while (roller[indeks] == null && indeks < roller.length - 1) indeks++;
            }
        }
        if (indeks == Rolle.MAFIA && roller[indeks] != null) {
            if (mafiaer == -1)
                mafiaer = roller[indeks].antall() - 1;
            else if (mafiaer > 0)
                mafiaer--;
            else {
                mafiaer = -1;
                indeks++;
                while (roller[indeks] == null && indeks < roller.length - 1) indeks++;
            }
        }
        if (indeks == Rolle.POLITI && roller[indeks] != null) {
            if (politi == -1)
                politi = roller[indeks].antall() - 1;
            else if (politi > 0)
                politi--;
            else {
                politi = -1;
                indeks++;
                while (roller[indeks] == null && indeks < roller.length - 1) {
                    if (indeks == roller.length - 1 && roller[roller.length - 1] == null)
                        nyfase(++fase);
                    indeks++;
                }
            }
        }

        if (indeks == roller.length - 1 && roller[roller.length - 1] == null && fase == HVEMERHVA) {
            nyfase(++fase);
            TvUtil.skjulBilde();
            TvUtil.lukkGuide();
        } else if (indeks < roller.length) {
            tekst.setText(roller[indeks].tittel() + " våkner");
            innhold.revalidate();
            if (TvUtil.tv.guideErSynlig())
                TvUtil.visGuide(roller[indeks].getGuide());
            if (indeks == Rolle.MAFIA)
                fordelSpesialister();
            else
                TvUtil.visRolleBilde(roller[indeks]);

        } else {
            nyfase(++fase);
            TvUtil.skjulBilde();
        }
    }

    public void nestePerson() {
        if (++personIndeks == spillere.spillere().size()) {
            nyfase(++fase);
            TvUtil.skjulBilde();
            TvUtil.lukkGuide();
            return;
        }
        Spiller spiller = spillere.spillere().get(personIndeks);
        tekst.setText(spiller.navn() + " våkner");
        SkjermUtil.fullLogg(spiller.navn() + " er " + spiller.rolle() + (spiller.getMafiarolle().isEmpty() ? "" : "(" + spiller.getMafiarolle() + ")"));
        if (spiller.rolle() instanceof Mafia)
            TvUtil.visMafiaRolleBilde((Mafia) spiller.rolle(), spiller.getMafiarolle());
        else
            TvUtil.visRolleBilde(spiller.rolle());
        if (TvUtil.tv.guideErSynlig())
            TvUtil.visGuide(spiller.rolle().getGuide());
    }

    public static Rolle hentRolle() {
        try {
            if (personIndeks >= 0 && personIndeks < spillere.spillere().size())
                return spillere.spillere().get(personIndeks).rolle();
            return roller[indeks];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public void setAntall(int a) {
        antallspillere = a;
    }

    public void setTid(int t) {
        tid = t;
    }

    public void informer(String informasjon) {
        TvUtil.vis(informasjon);
        SkjermUtil.fullLogg(informasjon);
    }

    private void visRoller(String rollerListe) {
        TvUtil.visRoller(rollerListe);
    }

    public void inverserKnapper() {
        fjerning = !fjerning;
        oppdaterKnapper();
    }

    public void oppdaterKnapper() {
        if (!fjerning && antallspillere == 0) {
            for (Component k : innhold.getComponents())
                if (k instanceof Knapp) k.setEnabled(false);
            return;
        }

        for (Component k : innhold.getComponents()) {
            Rolle r = ((Knapp) k).rolle();
            if (k.getSize().equals(Knapp.KVART)) {
                if (fjerning) {
                    k.setEnabled(harRolle(r));
                    if (r.id(Rolle.MAFIA) && !r.flere())
                        k.setEnabled(false);

                } else if (r.id(Rolle.POLITI) || r.id(Rolle.MAFIA) || r.id(Rolle.BESTEVENN))
                    k.setEnabled(true);
                else
                    k.setEnabled(!harRolle(r));
            }
        }
    }

    private boolean harRolle(Rolle rolle) {
        for (Rolle r : roller) {
            if (r != null && r.pri() == rolle.pri())
                return true;
        }
        return false;
    }

    private class Lytter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //////////////////////FORTSETT//////////////////////
            if (e.getSource() == vindu.getFortsett()) {
                if (fase == STARTSPILL) {
                    Spill spill = new Spill(vindu, roller, tid);
                    indeks = -1;
                    spill.natt();
                } else if (fase == HVEMERHVA) {
                    nestePerson();
                } else if (spillere.length() < 5)
                    informer("Ikke nok spillere!");
                else
                    nyfase(++fase);
            }

            //////////////////////TILBAKE//////////////////////
            else if (e.getSource() == vindu.getTilbake()) {
                if (fase == VELGROLLER) {
                    roller = null;
                    if (antallspillere < (spillere.length() - 1))
                        nyfase(fase);
                    else
                        nyfase(--fase);
                } else if (fase == VELGGJENSTANDER) {
                    if (!gjenstander.isEmpty()) {
                        gjenstander.clear();
                        nyfase(fase);
                    } else
                        nyfase(--fase);
                } else if (fase == VELGSPESIALISTER) {
                    if (mafia.fjernAlleSpesialister()) {
                        nyfase(fase);
                    } else
                        nyfase(--fase);
                } else if (fase == HVEMERHVA || fase == STARTSPILL) {
                    //Nullstill indeks for fordeling av roller
                    int i = indeks;
                    politi = -1;
                    mafiaer = -1;
                    venner = -1;
                    for (indeks = 0; roller[indeks] == null; indeks++) ;

                    if (mafia.nullstillSpesialister() || i != indeks || personIndeks >= 0) {
                        nyfase(HVEMERHVA);
                    } else {
                        nyfase(--fase);
                        TvUtil.skjulBilde();
                    }

                    personIndeks = -1;
                    fortsett.setText("Fortsett");
                    fortsett.setPreferredSize(Knapp.HEL);
                }
            }
            //////////////////////LEGG TIL ALLE//////////////////////
            else {
                leggTilSpiller(new Spiller("Sondre"));
                leggTilSpiller(new Spiller("Lars-Erik"));
                leggTilSpiller(new Spiller("Ørnulf"));
                leggTilSpiller(new Spiller("Daniel"));
                leggTilSpiller(new Spiller("Adrian"));
                //				spillere.leggTil(new Spiller("Kjetil"));
                //				spillere.leggTil(new Spiller("Jens Emil"));
                //				spillere.leggTil(new Spiller("Ole-Halvor"));
                //				spillere.leggTil(new Spiller("Randi"));
                //				spillere.leggTil(new Spiller("Bård Anders"));
                //				spillere.leggTil(new Spiller("Simon"));
                //				spillere.leggTil(new Spiller("Ole Martin"));
                //				spillere.leggTil(new Spiller("Emil"));
                //				spillere.leggTil(new Spiller("Ingrid"));
                //				spillere.leggTil(new Spiller("Anders"));
                //				spillere.leggTil(new Spiller("Andreas"));

				/* Trondheim
                spillere.leggTil(new Spiller("Sinte Simen"));
				spillere.leggTil(new Spiller("Ida Nigga Reite"));
				spillere.leggTil(new Spiller("Swigurd"));
				spillere.leggTil(new Spiller("Morten"));
				spillere.leggTil(new Spiller("Shiv"));
				spillere.leggTil(new Spiller("Stian"));
				spillere.leggTil(new Spiller("Jboy"));
				spillere.leggTil(new Spiller("DJ Sindre"));
				*/

                informer(spillere.toString());
            }
            vindu.velgIngen();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (fase == VELGSPILLERE) {
            if (spillere.finnSpillerMedNavn(navnefelt.getText()) == null) {
                Spiller spiller = new Spiller(navnefelt.getText().isEmpty() ? "Anonym" : navnefelt.getText());
                leggTilSpiller(spiller);
            } else {
                informer("Finnes allerede");
            }

            navnefelt.setText("");
            navnefelt.requestFocusInWindow();
            vindu.velgIngen();
            return;
        }

        Knapp k = (Knapp) e.getSource();

        if (fase == VELGROLLER) {

            int i = k.rolle().pri();

            if (fjerning) {
                if (roller[i] != null) {
                    if (roller[i].flere()) {
                        if (i == Rolle.POLITI)
                            ((Politi) roller[i]).fjern();
                        else if (i == Rolle.BESTEVENN)
                            ((Bestevenn) roller[i]).fjern();
                        else if (i == Rolle.MAFIA)
                            mafia.fjern();
                    } else
                        roller[i] = null;
                }

                informer(spillere.rolleString(roller, ++antallspillere));
                inverserKnapper();

                return;
            }

            if (roller[i] == null) {
                roller[i] = k.rolle();
                if (i != Rolle.POLITI && i != Rolle.MAFIA && i != Rolle.BESTEVENN)
                    k.setEnabled(false);
            } else if (i == Rolle.POLITI)
                ((Politi) roller[i]).fler();
            else if (i == Rolle.BESTEVENN)
                ((Bestevenn) roller[i]).fler();
            else if (i == Rolle.MAFIA)
                mafia.fler();

            informer(spillere.rolleString(roller, --antallspillere));
            if (antallspillere < 1) {
                nyfase(++fase);
            }
        } else if (fase == VELGSPESIALISTER) {
            String tekst = ((Knapp)e.getSource()).getText();
            k.setEnabled(false);
            if (mafia.leggTilSpesialist(tekst))
                vindu.getFortsett().doClick();
        } else if (fase == VELGGJENSTANDER) {
            gjenstander.add(k.getText());
            TvUtil.leggTil("\n" + k.getText());

            SkjermUtil.fullLogg(TvUtil.getText());

            if (gjenstander.size() == spillere.spillere().size()) {
                spillere.fordelGjenstander(gjenstander);
                nyfase(++fase);
            }
        } else if (fase == HVEMERHVA) {
            if (fordelerSpesialister) {
                k.spiller().setRolle(mafia, mafia.hentLedigSpesialist());
                stopSpesialistFordeling();
            } else
                k.spiller().setRolle(roller[indeks]);

            if (indeks != Rolle.POLITI && indeks != Rolle.MAFIA && indeks != Rolle.BESTEVENN)
                indeks++;

            nesteRolle();
            k.setEnabled(false);
        }
    }
}
