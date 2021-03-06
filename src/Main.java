import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Projet: Evolution
 *
 * @author Jordan Downey, Esteban Escandell
 * @version 1.0
 */
public class Main{
	public static void main(String[] args){
		new Menu();
	}
}

/**
 * Menu principal du projet
 *
 * Permet de charger une partie ou
 * de commencer une nouvelle partie
 * en definissant les parametres
 */
class Menu{
	public Menu(){
		new Fenetre();
	}
	
	/**
	 * Fenetre du menu principal
	 *
	 * Gestion de la disposition des elements
	 * de la fenetre du menu principal
	 */
	public class Fenetre extends JFrame implements ActionListener{
		private JLabel
			lab_ligne = new JLabel("Ligne: "),
			lab_colonne = new JLabel("Colonne: "),
			lab_mouton = new JLabel("Mouton: "),
			lab_loup = new JLabel("Loup: "),
			lab_reproduction = new JLabel("Durée de reproduction: ");
		
		private JTextField
			txt_ligne = new JTextField(5),
			txt_colonne = new JTextField(5),
			txt_mouton = new JTextField(5),
			txt_loup = new JTextField(5),
			txt_reproduction = new JTextField(5);
		
		private JButton
			btn_confirmer = new JButton("Confirmer"),
			btn_charger = new JButton("Charger");
		
		public Fenetre(){
			super();
			getContentPane().setLayout(new GridLayout(6, 2));
			
			getContentPane().add(lab_ligne);
			getContentPane().add(txt_ligne);
			
			getContentPane().add(lab_colonne);
			getContentPane().add(txt_colonne);
			
			getContentPane().add(lab_mouton);
			getContentPane().add(txt_mouton);
			
			getContentPane().add(lab_loup);
			getContentPane().add(txt_loup);
			
			getContentPane().add(lab_reproduction);
			getContentPane().add(txt_reproduction);
			
			btn_charger.addActionListener(this);
			getContentPane().add(btn_charger);
			
			btn_confirmer.addActionListener(this);
			getContentPane().add(btn_confirmer);
			
			pack();
			setSize(400, 150);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setResizable(false);
			setVisible(true);
		}
		
		/**
		 * Gestion de l'action des boutons options
		 */
		public void actionPerformed(ActionEvent e){
			Object source = e.getSource();
			
			if(source == btn_confirmer){
				String[] args = {txt_ligne.getText(), txt_colonne.getText(), txt_mouton.getText(), txt_loup.getText(), txt_reproduction.getText()};
				dispose();
				(new TLaunch(args)).start();
			}
			else if(source == btn_charger){
				dispose();
				(new TLaunch()).start();
			}
		}
	}
	
	/**
	 * Gestion des Threads
	 */
	public class TLaunch extends Thread{
		public String[] args;

		public void run(){
			if(args != null){
				new Plateau(args);
			}
			else{
				new Plateau();
			}
		}

		public TLaunch(){
			args = null;
		}

		public TLaunch(String[] a){
			args = a;
		}
	}
}

/**
 * Plateau de jeu du projet
 *
 * En haut a gauche: grille du jeu
 * En haut a droite: boutons options
 * En bas a gauche: slider de vitesse du jeu
 */
class Plateau extends JPanel{
	private Univers[][][] univers;
	
	private int
		ligne,
		colonne,
		mouton,
		loup,
		herbe,
		tour = 0,
		taille,
		reproduction,
		vitesse = 1,
		jeuPause = 1;
	
	private BufferedImage
		sheep,
		wolf,
		dead_sheep,
		dead_wolf,
		grass,
		dirt,
		heart;
	
	private JSlider sliders = new JSlider();
	
	private JButton
		pause = new JButton("Pause"),
		continuer = new JButton("Continuer"),
		quitter = new JButton("Quitter"),
		menu = new JButton("Menu"),
		sauver = new JButton("Sauver"),
		charger = new JButton("Charger");
	
	public Plateau(String[] args){
		super();
		try{
			ligne = Integer.parseInt(args[0]);
			colonne = Integer.parseInt(args[1]);
			mouton = Integer.parseInt(args[2]);
			loup = Integer.parseInt(args[3]);
			reproduction = Integer.parseInt(args[4]);
			
			if(ligne <= 0 || colonne <= 0 || mouton < 0 || loup < 0 || ligne*colonne < mouton+loup || reproduction < 0){
				throw new Exception();
			}
			
			herbe = ligne*colonne-mouton-loup;
			univers = new Univers[colonne][ligne][2];
			taille = Math.min(600/ligne, 400/colonne);
			
			sheep = resizeImage(ImageIO.read(new File("Sprites/Sheep.png")), taille, taille);
			wolf = resizeImage(ImageIO.read(new File("Sprites/Wolf.png")), taille, taille);
			dead_sheep = resizeImage(ImageIO.read(new File("Sprites/Dead_sheep.png")), taille, taille);
			dead_wolf = resizeImage(ImageIO.read(new File("Sprites/Dead_wolf.png")), taille, taille);
			grass = resizeImage(ImageIO.read(new File("Sprites/Grass.png")), 600/ligne, 400/colonne);
			dirt = resizeImage(ImageIO.read(new File("Sprites/Dirt.png")), 600/ligne, 400/colonne);
			heart = resizeImage(ImageIO.read(new File("Sprites/Heart.png")), taille/3, taille/3);
		}
		catch(Exception e){
			System.err.println("Erreur : Parametres invalides");
			System.exit(0);
		}
		jeu(true);
	}
	
	public Plateau(){
		super();
		try{
			charger();
		}
		catch(IOException ex){
			System.err.println("Erreur : Parametres invalides");
			System.exit(0);
		}
		jeu(false);
	}
	
	/**
	 * Fenetre du plateau de jeu
	 *
	 * Gestion de la disposition des elements
	 * de la fenetre du plateau de jeu
	 */
	public class Fenetre extends JFrame implements ActionListener, ChangeListener{
		public Fenetre(Plateau p){
			super();
			getContentPane().setLayout(null);
			
			p.setBounds(0, 0, 600, 400);
			getContentPane().add(p);
			
			sliders = new JSlider(1, 10, 1);
			sliders.setMajorTickSpacing(1);
			sliders.setMinorTickSpacing(1);
			sliders.setPaintTicks(true);
			sliders.setPaintLabels(true);
			sliders.setBorder(BorderFactory.createTitledBorder("Vitesse"));
			sliders.addChangeListener(this);
			sliders.setBounds(50, 425, 500, 100);
			getContentPane().add(sliders);
			
			pause.setBounds(635, 50, 125, 25);
			pause.addActionListener(this);
			getContentPane().add(pause);
			
			menu.setBounds(635, 80, 125, 25);
			menu.addActionListener(this);
			getContentPane().add(menu);
			
			sauver.setBounds(635, 110, 125, 25);
			sauver.addActionListener(this);
			getContentPane().add(sauver);
			
			charger.setBounds(635, 140, 125, 25);
			charger.addActionListener(this);
			getContentPane().add(charger);
			
			quitter.setBounds(635, 170, 125, 25);
			quitter.addActionListener(this);
			getContentPane().add(quitter);
			
			pack();
			setSize(800,600);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setResizable(false);
			setVisible(true);
		}
		
		/**
		 * Gestion de l'action du slider de vitesse
		 */
		public void stateChanged(ChangeEvent e){
			JSlider source = (JSlider)e.getSource();
			if(source == sliders){
				vitesse = (int)source.getValue();
			}
		}
		
		/**
		 * Gestion de l'action des boutons options
		 */
		public void actionPerformed(ActionEvent e){
			Object source = e.getSource();
			
			if(source == pause){
				jeuPause = 0;
				getContentPane().remove(pause);
				continuer.setBounds(635, 50, 125, 25);
				continuer.addActionListener(this);
				getContentPane().add(continuer);
			}
			else if(source == continuer){
				jeuPause = 1;
				getContentPane().remove(continuer);
				pause.setBounds(635, 50, 125, 25);
				getContentPane().add(pause);
			}
			else if(source == menu){
				jeuPause = 2;
				dispose();
				new Menu();
			}
			else if(source == quitter){
				jeuPause = 2;
				dispose();
			}
			else if(source == sauver){
				pause.doClick();
				try{sauvegarder();}catch(IOException ex){quitter.doClick();}
			}
			else if(source == charger){
				pause.doClick();
				try{charger();}catch(IOException ex){}
			}
			
			revalidate();
			repaint();
		}
	}
	
	/**
	 * Sauvegarde d'une partie dans un fichier
	 *
	 * Si le fichier existe deja, il est remplace
	 * sinon, il est cree
	 */
	public void sauvegarder() throws IOException{
		JFileChooser fc_sauver = new JFileChooser(new File("."));
		fc_sauver.setApproveButtonText("Sauver");
		PrintWriter sortie;
		File fichier;
		
		if(fc_sauver.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
			fichier = fc_sauver.getSelectedFile();
			sortie = new PrintWriter(new FileWriter(fichier.getPath(), false));
			
			sortie.println(colonne+" "+ligne+" "+tour+" "+reproduction);
			
			for(int i = 0; i < colonne; i++){
				for(int j = 0; j < ligne; j++){
					if(univers[i][j][0] instanceof Mouton){
						sortie.println("M "+((Animal)univers[i][j][0]).getFaim()+" "+((Animal)univers[i][j][0]).getVie()+" "
										 +((Animal)univers[i][j][0]).getSexe()+" "+((Animal)univers[i][j][0]).getReproduction());
					}
					else if(univers[i][j][0] instanceof Loup){
						sortie.println("L "+((Animal)univers[i][j][0]).getFaim()+" "+((Animal)univers[i][j][0]).getVie()+" "
										 +((Animal)univers[i][j][0]).getSexe()+" "+((Animal)univers[i][j][0]).getReproduction()+" "+((Animal)univers[i][j][0]).getSurHerbe());
					}
					else if(univers[i][j][0] instanceof Herbe){
						sortie.println('H');
					}
					else if(univers[i][j][0] instanceof Sels){
						sortie.println("S "+((Sels)univers[i][j][0]).getType()+" "+((Sels)univers[i][j][0]).getSurHerbe());
					}
					else{
						sortie.println('V');
					}
				}
			}
			
			sortie.println();
			
			sortie.close();
		}
	}
	
	/**
	 * Chargement d'une partie d'un fichier
	 */
	public void charger() throws IOException{
		JFileChooser fc_charger = new JFileChooser(new File("."));
		fc_charger.setApproveButtonText("Charger");
		BufferedReader entree;
		File fichier;
		
		if(fc_charger.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
			fichier = fc_charger.getSelectedFile();
			
			try{
				entree = new BufferedReader(new FileReader(fichier.getPath()));
				
				String line = entree.readLine();
				String[] param = line.split(" ");
				
				colonne = Integer.parseInt(param[0]);
				ligne = Integer.parseInt(param[1]);
				tour = Integer.parseInt(param[2]);
				reproduction = Integer.parseInt(param[3]);
				
				mouton = 0;
				loup = 0;
				univers = new Univers[colonne][ligne][2];
				taille = Math.min(600/ligne, 400/colonne);
				
				sheep = resizeImage(ImageIO.read(new File("Sprites/Sheep.png")), taille, taille);
				wolf = resizeImage(ImageIO.read(new File("Sprites/Wolf.png")), taille, taille);
				dead_sheep = resizeImage(ImageIO.read(new File("Sprites/Dead_sheep.png")), taille, taille);
				dead_wolf = resizeImage(ImageIO.read(new File("Sprites/Dead_wolf.png")), taille, taille);
				grass = resizeImage(ImageIO.read(new File("Sprites/Grass.png")), 600/ligne, 400/colonne);
				dirt = resizeImage(ImageIO.read(new File("Sprites/Dirt.png")), 600/ligne, 400/colonne);
				heart = resizeImage(ImageIO.read(new File("Sprites/Heart.png")), taille/3, taille/3);
				
				for(int i = 0; i < colonne; i++){
					for(int j = 0; j < ligne; j++){
						line = entree.readLine();
						param = line.split(" ");
						
						if(param[0].equals("M")){
							univers[i][j][1] = new Mouton(Integer.parseInt(param[1]), Integer.parseInt(param[2]), Integer.parseInt(param[3]), Integer.parseInt(param[4]));
							mouton++;
						}
						else if(param[0].equals("L")){
							univers[i][j][1] = new Loup(Integer.parseInt(param[1]), Integer.parseInt(param[2]), Integer.parseInt(param[3]),
														Integer.parseInt(param[4]), Boolean.parseBoolean(param[5]));
							loup++;
						}
						else if(param[0].equals("H")){
							univers[i][j][0] = new Herbe();
						}
						else if(param[0].equals("S")){
							univers[i][j][0] = new Sels(param[1].charAt(0), Boolean.parseBoolean(param[2]));
						}
						else{
							univers[i][j][0] = null;
						}
					}
				}
				
				if(ligne <= 0 || colonne <= 0 || mouton < 0 || loup < 0 || ligne*colonne < mouton+loup || reproduction < 0){
					throw new Exception();
				}
				
				entree.close();
			}
			catch(Exception ex){
				System.err.println("Erreur : Parametres invalides");
				System.exit(0);
			}
		}
		
		miseAJour();
	}
	
	/**
	 * Affichage graphique du projet
	 */
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		for(int i = 0; i < colonne; i++){
			g.drawLine(0, i*400/colonne, 600, i*400/colonne);
			
			for(int j = 0; j < ligne; j++){
				g.drawLine(j*600/ligne, 0, j*600/ligne, 400);
				
				if(univers[i][j][0] instanceof Mouton){
					if(((Animal)univers[i][j][0]).getSurHerbe()){
						g.drawImage(grass, j*600/ligne+1, i*400/colonne+1, this);
					}
					else{
						g.drawImage(dirt, j*600/ligne+1, i*400/colonne+1, this);
					}
					
					g.drawImage(sheep, j*600/ligne+1, i*400/colonne+1, this);
					
					if(((Animal)univers[i][j][0]).getReproduction() == -1){
						g.drawImage(heart, j*600/ligne+1, i*400/colonne+1, this);
					}
				}
				else if(univers[i][j][0] instanceof Loup){
					if(((Animal)univers[i][j][0]).getSurHerbe()){
						g.drawImage(grass, j*600/ligne+1, i*400/colonne+1, this);
					}
					else{
						g.drawImage(dirt, j*600/ligne+1, i*400/colonne+1, this);
					}
					
					g.drawImage(wolf, j*600/ligne+1, i*400/colonne+1, this);
					
					if(((Animal)univers[i][j][0]).getReproduction() == -1){
						g.drawImage(heart, j*600/ligne+1, i*400/colonne+1, this);
					}
				}
				else if(univers[i][j][0] instanceof Herbe){
					g.drawImage(grass, j*600/ligne+1, i*400/colonne+1, this);
				}
				else if(univers[i][j][0] instanceof Sels){
					if(((Sels)univers[i][j][0]).getSurHerbe()){
						g.drawImage(grass, j*600/ligne+1, i*400/colonne+1, this);
					}
					else{
						g.drawImage(dirt, j*600/ligne+1, i*400/colonne+1, this);
					}
					
					if(((Sels)univers[i][j][0]).getType() == 'M'){
						g.drawImage(dead_sheep, j*600/ligne+1, i*400/colonne+1, this);
					}
					else{
						g.drawImage(dead_wolf, j*600/ligne+1, i*400/colonne+1, this);
					}
				}
				else{
					g.drawImage(dirt, j*600/ligne+1, i*400/colonne+1, this);
				}
			}
			
			g.drawLine(600, 0, 600, 400);
		}
		
		g.drawLine(0, 400, 600, 400);
	}
	
	/**
	 * Gestion du redimentionnement des images
	 *
	 * @author JiDuL (jidul.com)
	 *
	 * @param original image a redimentionner
	 * @param width largeur souhaitee
	 * @param heigth hauteur souhaitee
	 *
	 * @return newImage image redimentionnee
	 */
	public static BufferedImage resizeImage(BufferedImage original, int width, int height){
		BufferedImage newImage = new BufferedImage(width, height, original.getType());
		Graphics2D g = newImage.createGraphics();
		
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.drawImage(original, 0, 0, width, height, null);
		g.dispose();
		
		return newImage;
	}
	
	/**
	 * Deroulement principal du jeu
	 *
	 * @param b false s'il s'agit d'une partie chargee, true sinon
	 */
	public void jeu(boolean b){
		if(b){
			init();
			miseAJour();
		}
		else{
			new Fenetre(this);
		}
		
		affiche();
		while(!fin()){
			try{
				Thread.sleep(5000/vitesse);
			}
			catch(InterruptedException ex){}
			if(jeuPause == 1){
				mouvement();
				miseAJour();
				affiche();
			}
			else if(jeuPause == 2){
				return;
			}
		}
	}
	
	/**
	 * Initialisation de la partie
	 */
	public void init(){
		int
			tmpMout = mouton,
			tmpLoup = loup,
			tmpHerb = herbe;
		
		for(int i = 0; i < colonne; i++){
			for(int j = 0; j < ligne; j++){
				int r = (int)(Math.random()*(tmpMout+tmpLoup+tmpHerb)+1);
				
				univers[i][j][0] = new Herbe();
				
				if(r <= tmpMout && tmpMout > 0){
					univers[i][j][1] = new Mouton(reproduction);
					tmpMout--;
				}
				else if(r <= tmpMout+tmpLoup && tmpLoup > 0){
					univers[i][j][1] = new Loup(reproduction);
					tmpLoup--;
				}
				else{
					tmpHerb--;
				}
			}
		}
		new Fenetre(this);
	}
	
	/**
	 * Affichage texte du projet
	 */
	public void affiche(){
		for(int i = 0; i <= colonne*2+1; i++){
			if(i == 0){
				System.out.print("   ");
			}
			else if(i%2 != 0){
				System.out.print("  +");
			}
			else{
				System.out.print((char)(i/2+64)+" |");
			}
			
			for(int j = 1; j <= ligne; j++){
				if(i == 0){
					System.out.print(" "+j+"  ");
				}
				else if(i%2 != 0){
					System.out.print("---+");
				}
				else if(univers[i/2-1][j-1][0] instanceof Mouton){
					System.out.print(" M |");
				}
				else if(univers[i/2-1][j-1][0] instanceof Loup){
					System.out.print(" L |");
				}
				else if(univers[i/2-1][j-1][0] instanceof Herbe){
					System.out.print(" H |");
				}
				else if(univers[i/2-1][j-1][0] instanceof Sels){
					System.out.print(" S |");
				}
				else{
					System.out.print("   |");
				}
			}
			
			System.out.println();
		}
		
		System.out.println("\nTour "+tour+" | Loups : "+loup+" | Moutons : "+mouton+"\n-----------------------------------");
	}
	
	/**
	 * Condition de fin de partie
	 *
	 * @return true s'il n'y a plus d'animaux en jeu, false sinon
	 */
	public boolean fin(){
		return mouton == 0 && loup == 0;
	}
	
	/**
	 * Gestion de deplacement valide
	 *
	 * @param i ordonnees
	 * @param j abcisses
	 *
	 * @return alea coordonnees d'un deplacement valide
	 */
	public int[] deplacementValide(int i, int j){
		int[] alea = {(int)(Math.random()*4-2), (int)(Math.random()*4-2)};
		
		while(i+alea[0] < 0 || i+alea[0] >= colonne || j+alea[1] < 0 || j+alea[1] >= ligne){
			alea[0] = (int)(Math.random()*4-2);
			alea[1] = (int)(Math.random()*4-2);
		}
		
		return alea;
	}
	
	public int zoneDeDanger(int i, int j, int k, int l){
		if(univers[i+k][j+l][0] instanceof Loup || univers[i+k][j+l][1] instanceof Loup){
			return 12;
		}
		else if(univers[i+k][j+l][0] instanceof Sels && ((Sels)univers[i+k][j+l][0]).getType() == 'M'){
			return 4;
		}
		else if((univers[i+k][j+l][0] instanceof Mouton && ((Animal)univers[i+k][j+l][0]).getSexe() != ((Animal)univers[i][j][0]).getSexe()) ||
				(univers[i+k][j+l][1] instanceof Mouton && ((Animal)univers[i+k][j+l][1]).getSexe() != ((Animal)univers[i][j][0]).getSexe())){
			return -6;
		}
		else if(univers[i+k][j+l][0] instanceof Herbe){
			return -4;
		}
		else{
			return 0;
		}
	}
	
	public int zoneDeChasse(int i, int j, int k, int l){
		if(univers[i+k][j+l][0] instanceof Mouton || univers[i+k][j+l][1] instanceof Mouton){
			return 12;
		}
		else if((univers[i+k][j+l][0] instanceof Loup && ((Animal)univers[i+k][j+l][0]).getSexe() != ((Animal)univers[i][j][0]).getSexe()) ||
				(univers[i+k][j+l][1] instanceof Loup && ((Animal)univers[i+k][j+l][1]).getSexe() != ((Animal)univers[i][j][0]).getSexe())){
			return 4;
		}
		else if(univers[i+k][j+l][0] instanceof Loup || univers[i+k][j+l][1] instanceof Loup){
			return 2;
		}
		else if(univers[i+k][j+l][0] instanceof Sels && ((Sels)univers[i+k][j+l][0]).getType() == 'L'){
			return -4;
		}
		else{
			return 0;
		}
	}
	
	/**
	 * Gestion des mouvements des animaux
	 */
	public void mouvement(){
		for(int i = 0; i < colonne; i++){
			for(int j = 0; j < ligne; j++){
				if(univers[i][j][0] instanceof Animal){
					if(((Animal)univers[i][j][0]).getReproduction() >= 0){
						int[] mouv = new int[2];
						// Debut Aleatoire
						mouv = deplacementValide(i, j);
						int b = 1000;
						
						while((univers[i][j][0] instanceof Mouton && (univers[i+mouv[0]][j+mouv[1]][1] instanceof Mouton ||
							  (univers[i+mouv[0]][j+mouv[1]][0] instanceof Mouton && ((Animal)univers[i+mouv[0]][j+mouv[1]][0]).getReproduction() == -1))) ||
							  (univers[i][j][0] instanceof Loup && (univers[i+mouv[0]][j+mouv[1]][1] instanceof Loup ||
							  (univers[i+mouv[0]][j+mouv[1]][0] instanceof Loup && ((Animal)univers[i+mouv[0]][j+mouv[1]][0]).getReproduction() == -1))) && b != 0){
							mouv = deplacementValide(i, j);
							b--;
						}
						
						if(b == 0){
							if(univers[i][j][0] instanceof Mouton){
								mouton--;
							}
							else{
								loup--;
							}
							univers[i][j][0] = null;
							break;
						}
						// Fin aleatoire
						
						/*
						// Debut intelligent
						int[][] danger = new int[3][3];
						
						if(univers[i][j][0] instanceof Mouton){
							for(int k = -1; k < 2; k++){
								for(int l = -1; l < 2; l++){
									if(k != 0 && i+k >= 0 && i+k < colonne && l != 0 && j+l >= 0 && j+l < ligne){
										int val = zoneDeDanger(i, j, k, l);
										if(k <= 0 && l <= 0){
											danger[0][0] += val;
											danger[0][1] += val/2;
											danger[1][0] += val/2;
										}
										else if(k <= 0 && l == 0){
											danger[0][1] += val;
											danger[0][0] += val/2;
											danger[0][2] += val/2;
										}
										else if(k <= 0 && l >= 0){
											danger[0][2] += val;
											danger[0][1] += val/2;
											danger[1][2] += val/2;
										}
										else if(k == 0 && l <= 0){
											danger[1][0] += val;
											danger[0][0] += val/2;
											danger[2][0] += val/2;
										}
										else if(k == 0 && l == 0){
											danger[1][1] += val;
										}
										else if(k == 0 && l >= 0){
											danger[1][2] += val;
											danger[0][2] += val/2;
											danger[2][2] += val/2;
										}
										else if(k >= 0 && l <= 0){
											danger[2][0] += val;
											danger[1][0] += val/2;
											danger[2][0] += val/2;
										}
										else if(k >= 0 && l == 0){
											danger[2][1] += val;
											danger[2][0] += val/2;
											danger[2][2] += val/2;
										}
										else if(k >= 0 && l >= 0){
											danger[2][2] += val;
											danger[2][1] += val/2;
											danger[1][2] += val/2;
										}
									}
								}
							}
							
							int minimum = danger[1][1];
							mouv[0] = 0;
							mouv[1] = 0;
							
							for(int k = 0; k < danger.length; k++){
								for(int l = 0; l < danger[k].length; l++){
									if(i+k-1 >= 0 && i+k-1 < colonne && j+l-1 >= 0 && j+l-1 < ligne && danger[k][l] < minimum &&
									   !(univers[i+k-1][j+l-1][1] instanceof Mouton || (univers[i+k-1][j+l-1][0] instanceof Mouton && ((Animal)univers[i+k-1][j+l-1][0]).getReproduction() == -1))){
										minimum = danger[k][l];
										mouv[0] = k-1;
										mouv[1] = l-1;
									}
								}
							}
						}
						
						else if(univers[i][j][0] instanceof Loup){
							for(int k = -1; k < 2; k++){
								for(int l = -1; l < 2; l++){
									if(k != 0 && i+k >= 0 && i+k < colonne && l != 0 && j+l >= 0 && j+l < ligne){
										int val = zoneDeChasse(i, j, k, l);
										if(k <= 0 && l <= 0){
											danger[0][0] += val;
											danger[0][1] += val/2;
											danger[1][0] += val/2;
										}
										else if(k <= 0 && l == 0){
											danger[0][1] += val;
											danger[0][0] += val/2;
											danger[0][2] += val/2;
										}
										else if(k <= 0 && l >= 0){
											danger[0][2] += val;
											danger[0][1] += val/2;
											danger[1][2] += val/2;
										}
										else if(k == 0 && l <= 0){
											danger[1][0] += val;
											danger[0][0] += val/2;
											danger[2][0] += val/2;
										}
										else if(k == 0 && l == 0){
											danger[1][1] = 0;
										}
										else if(k == 0 && l >= 0){
											danger[1][2] += val;
											danger[0][2] += val/2;
											danger[2][2] += val/2;
										}
										else if(k >= 0 && l <= 0){
											danger[2][0] += val;
											danger[1][0] += val/2;
											danger[2][0] += val/2;
										}
										else if(k >= 0 && l == 0){
											danger[2][1] += val;
											danger[2][0] += val/2;
											danger[2][2] += val/2;
										}
										else if(k >= 0 && l >= 0){
											danger[2][2] += val;
											danger[2][1] += val/2;
											danger[1][2] += val/2;
										}
									}
								}
							}
							
							int maximum = danger[1][1];
							mouv[0] = 0;
							mouv[1] = 0;
							
							for(int k = 0; k < danger.length; k++){
								for(int l = 0; l < danger[k].length; l++){
									if(i+k-1 >= 0 && i+k-1 < colonne && j+l-1 >= 0 && j+l-1 < ligne && danger[k][l] > maximum &&
									   !(univers[i+k-1][j+l-1][1] instanceof Loup || (univers[i+k-1][j+l-1][0] instanceof Loup && ((Animal)univers[i+k-1][j+l-1][0]).getReproduction() == -1))){
										maximum = danger[k][l];
										mouv[0] = k-1;
										mouv[1] = l-1;
									}
								}
							}
						}
						// Fin intelligent
						*/
						
						if(univers[i+mouv[0]][j+mouv[1]][1] instanceof Loup && univers[i][j][0] instanceof Mouton){
							System.out.println("["+(char)(i+65)+(j+1)+"] : 1 : Le mouton se fait manger par le loup");
							((Animal)univers[i+mouv[0]][j+mouv[1]][1]).manger();
							univers[i][j][0] = null;
							mouton--;
						}
						else if(univers[i+mouv[0]][j+mouv[1]][1] instanceof Mouton && univers[i][j][0] instanceof Loup){
							System.out.println("["+(char)(i+65)+(j+1)+"] : 1 : Le loup mange le mouton");
							((Animal)univers[i][j][0]).manger();
							univers[i+mouv[0]][j+mouv[1]][1] = univers[i][j][0];
							univers[i][j][0] = null;
							mouton--;
						}
						else if(univers[i][j][0] instanceof Loup && ((Animal)univers[i][j][0]).getSurHerbe()){
							((Animal)univers[i][j][0]).setSurHerbe(false);
							univers[i+mouv[0]][j+mouv[1]][1] = univers[i][j][0];
							univers[i][j][0] = new Herbe();
						}
						else{
							univers[i+mouv[0]][j+mouv[1]][1] = univers[i][j][0];
							univers[i][j][0] = null;
						}
						
						if(((Animal)univers[i+mouv[0]][j+mouv[1]][1]).getReproduction() > 0){
							((Animal)univers[i+mouv[0]][j+mouv[1]][1]).setReproduction(((Animal)univers[i+mouv[0]][j+mouv[1]][1]).getReproduction()-1);
						}
					}
					else{
						if(univers[i][j][0] instanceof Mouton && univers[i][j][1] instanceof Loup){
							System.out.println("["+(char)(i+65)+(j+1)+"] : 2 : Le loup mange le mouton");
							((Animal)univers[i][j][1]).manger();
							univers[i][j][0] = null;
							mouton--;
						}
						else if(univers[i][j][0] instanceof Loup && univers[i][j][1] instanceof Mouton){
							System.out.println("["+(char)(i+65)+(j+1)+"] : 2 : Le mouton se fait manger par le loup");
							((Animal)univers[i][j][0]).manger();
							mouton--;
						}
						
						univers[i][j][1] = univers[i][j][0];
						univers[i][j][0] = null;
					}
				}
			}
		}
	}
	
	/**
	 * Gestion des actions environnementales
	 */
	public void miseAJour(){
		for(int i = 0; i < colonne; i++){
			for(int j = 0; j < ligne; j++){
				if(univers[i][j][0] instanceof Sels){
					System.out.println("["+(char)(i+65)+(j+1)+"] : 1 : De l'herbe repousse");
					univers[i][j][0] = new Herbe();
				}
				
				if((univers[i][j][0] instanceof Herbe && univers[i][j][1] instanceof Mouton)){
					System.out.println("["+(char)(i+65)+(j+1)+"] : 1 : Le mouton mange de l'herbe");
					((Animal)univers[i][j][1]).manger();
					univers[i][j][0] = null;
				}
				
				if((univers[i][j][0] instanceof Mouton && univers[i][j][1] instanceof Loup)){
					System.out.println("["+(char)(i+65)+(j+1)+"] : 3 : Le loup mange le mouton");
					((Animal)univers[i][j][1]).manger();
					univers[i][j][0] = null;
				}
				
				if(univers[i][j][1] instanceof Animal){
					if(((Animal)univers[i][j][1]).getReproduction() < 0){
						if(((Animal)univers[i][j][1]).getSexe() == 0){
							for(int k = -1; k < 2; k++){
								for(int l = -1; l < 2; l++){
									if(!(k == 0 && l == 0) && i+k >= 0 && i+k < colonne && j+l >= 0 && j+l < ligne && (k+l == -1 || k+l == 1)){
										int tmp;
										
										if(k+l < 0){
											tmp = 0;
										}
										else{
											tmp = 1;
										}
										
										if(univers[i][j][1] instanceof Mouton && !(univers[i+k][j+l][tmp] instanceof Mouton)){
											System.out.println("["+(char)(i+k+65)+(j+l+1)+"] : 1 : Un mouton est ne");
											
											if(univers[i+k][j+l][tmp] instanceof Loup){
												System.out.println("["+(char)(i+k+65)+(j+l+1)+"] : 4 : Le loup mange le mouton");
												((Animal)univers[i+k][j+l][tmp]).manger();
											}
											else{
												univers[i+k][j+l][0] = new Mouton(reproduction);
												mouton++;
												
												if(univers[i+k][j+l][0] instanceof Herbe){
													((Animal)univers[i+k][j+l][1]).setSurHerbe(true);
												}
											}
											k = 2;
											l = 2;
										}
										else if(univers[i][j][1] instanceof Loup && !(univers[i+k][j+l][tmp] instanceof Loup)){
											System.out.println("["+(char)(i+k+65)+(j+l+1)+"] : 1 : Un loup est ne");
											univers[i+k][j+l][0] = new Loup(reproduction);
											loup++;
											
											if(univers[i+k][j+l][tmp] instanceof Mouton){
												System.out.println("["+(char)(i+k+65)+(j+l+1)+"] : 3 : Le mouton se fait manger par le loup");
												((Animal)univers[i+k][j+l][1]).manger();
											}
											else if(univers[i+k][j+l][0] instanceof Herbe){
												((Animal)univers[i+k][j+l][1]).setSurHerbe(true);
											}
											k = 2;
											l = 2;
										}
									}
								}
							}
							
							((Animal)univers[i][j][1]).setReproduction(reproduction);
						}
						else{
							((Animal)univers[i][j][1]).setReproduction(0);
						}
					}
					else{
						for(int k = -1; k < 1; k++){
							for(int l = -1; l < 1; l++){
								if(i+k >= 0 && i+k < colonne && j+l >= 0 && j+l < ligne && (k+l == -1 || k+l == 1) && univers[i+k][j+l][0] instanceof Animal &&
								   ((Animal)univers[i][j][1]).getSexe() != ((Animal)univers[i+k][j+l][0]).getSexe() &&
								   ((univers[i][j][1] instanceof Mouton && univers[i+k][j+l][0] instanceof Mouton) ||
								   (univers[i][j][1] instanceof Loup && univers[i+k][j+l][0] instanceof Loup)) &&
								   ((Animal)univers[i+k][j+l][0]).getReproduction() == 0 && ((Animal)univers[i][j][1]).getReproduction() == 0){
									System.out.println("["+(char)(i+65)+(j+1)+"] se reproduit avec ["+(char)(i+k+65)+(j+l+1)+"]");
									((Animal)univers[i][j][1]).setReproduction(-1);
									((Animal)univers[i+k][j+l][0]).setReproduction(-1);
								}
							}
						}
					}
					
					((Animal)univers[i][j][1]).setFaim(((Animal)univers[i][j][1]).getFaim()-1);
					((Animal)univers[i][j][1]).setVie(((Animal)univers[i][j][1]).getVie()-1);
					
					if(!((Animal)univers[i][j][1]).vivant()){
						if(univers[i][j][1] instanceof Mouton){
							System.out.println("["+(char)(i+65)+(j+1)+"] : 1 : Un mouton est mort");
							mouton--;
							univers[i][j][1] = new Sels('M');
						}
						else{
							System.out.println("["+(char)(i+65)+(j+1)+"] : 1 : Un loup est mort");
							loup--;
							univers[i][j][1] = new Sels('L');
						}
					}
				}
				
				if(univers[i][j][1] instanceof Loup && univers[i][j][0] instanceof Herbe){
					((Animal)univers[i][j][1]).setSurHerbe(true);
				}
				else if(univers[i][j][1] instanceof Sels && univers[i][j][0] instanceof Herbe){
					((Sels)univers[i][j][1]).setSurHerbe(true);
				}
				else if(univers[i][j][0] instanceof Herbe){
					univers[i][j][1] = univers[i][j][0];
				}
				
				univers[i][j][0] = univers[i][j][1];
				univers[i][j][1] = null;
			}
		}
		
		tour++;
		System.out.println();
		repaint();
	}
}

/**
 * Univers: animal + vegetal
 */
class Univers{}

/**
 * Animal: mouton + loup
 */
class Animal extends Univers{
	private int
		faim,
		faimMax,
		vie,
		sexe,
		reproduction;
	
	private boolean surHerbe;

	public Animal(int f, int v, int r){
		faim = f;
		faimMax = f;
		vie = v;
		sexe = (int)(Math.random()*2);
		
		if(sexe == 0){
			reproduction = r;
		}
		else{
			reproduction = 0;
		}
		
		surHerbe = false;
	}

	public Animal(int f, int fm, int v, int s, int r, boolean sH){
		faim = f;
		faimMax = fm;
		vie = v;
		sexe = s;
		reproduction = r;
		surHerbe = sH;
	}
	
	// Fonctions de recuperation de valeurs
	public int getFaim(){ return faim; }
	public int getVie(){ return vie; }
	public int getSexe(){ return sexe; }
	public int getReproduction(){ return reproduction; }
	public boolean getSurHerbe(){ return surHerbe; }
	
	// Fonctions de modification de valeurs
	public void setFaim(int f){ faim = f; }
	public void setVie(int v){ vie = v; }
	public void setReproduction(int r){ reproduction = r; }
	public void setSurHerbe(boolean sh){ surHerbe = sh; }
	
	/**
	 * Gestion de l'alimentation des animaux
	 */
	public void manger(){
		faim = faimMax;
	}
	
	/**
	 * Condition de vie d'un animal
	 *
	 * @return false si l'animal est mort de faim ou de vieillesse, true sinon
	 */
	public boolean vivant(){
		return faim > 0 && vie > 0;
	}
}

/**
 * Mouton
 */
class Mouton extends Animal{
	public Mouton(int r){
		super(5, 50, r);
	}
	
	public Mouton(int f, int v, int s, int r){
		super(f, 5, v, s, r, false);
	}
}

/**
 * Loup
 */
class Loup extends Animal{
	public Loup(int r){
		super(10, 60, r);
	}
	
	public Loup(int f, int v, int s, int r, boolean sH){
		super(f, 10, v, s, r, sH);
	}
}

/**
 * Vegetal: herbe + sels mineraux
 */
class Vegetal extends Univers{}

/**
 * Herbe
 */
class Herbe extends Vegetal{}

/**
 * Sels mineraux
 */
class Sels extends Vegetal{
	private char type;
	private boolean surHerbe;
	
	public Sels(char t){
		type = t;
		surHerbe = false;
	}
	
	public Sels(char t, boolean sH){
		type = t;
		surHerbe = sH;
	}
	
	// Fonctions de recuperation de valeurs
	public boolean getSurHerbe(){ return surHerbe; }
	public char getType(){ return type; }
	
	// Fonctions de modification de valeurs
	public void setSurHerbe(boolean sh){ surHerbe = sh; }
}
