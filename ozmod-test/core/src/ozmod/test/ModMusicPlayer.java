package ozmod.test;

import ozmod.ITPlayer;
import ozmod.LoaderFromMemory;
import ozmod.MODPlayer;
import ozmod.OZMod;
import ozmod.PipeIn;
import ozmod.S3MPlayer;
import ozmod.XMPlayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class ModMusicPlayer {
	OZMod ozm;
	S3MPlayer player;
	private Runnable nextSong = new Runnable() {
		@Override
		public void run() {
			System.gc();
			try {
				//sleep 1 second between songs
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			System.out.println("* nextSong");
			play(volume);
		}
	};
	private float volume;
	final private Array<FileHandle> playlist = new Array<FileHandle>();
	final private Array<FileHandle> currentlist = new Array<FileHandle>();

	public void pause(){
		if (player!=null) {
//			player.pause(true);
		}
	}
	
	public void resume(){
		if (player!=null) {
//			player.pause(false);
		}
	}
	
	public ModMusicPlayer() {
		ozm = new OZMod();
		ozm.initOutput();
	}
	
	public void loadUsingPlist(){
		playlist.clear();
		FileHandle plist = Gdx.files.internal("mods/list.txt");
		String[] list = plist.readString("UTF-8").split("\n");
		for (String mod: list) {
			Gdx.app.log("ModMusicPlayer: ", mod);
			FileHandle file = Gdx.files.internal("mods/"+mod);
			playlist.add(file);
		}
	}
	public Array<FileHandle> getPlaylist() {
		return playlist;
	}

	public void play(float _volume) {
		if (currentlist.size==0) {
			reloadActiveList();
		}
		if (playlist.size==0) {
			return;
		}		
		volume = _volume;		
		FileHandle nextMod = currentlist.get(0); 
		System.out.println("Playing: "+nextMod.nameWithoutExtension());
		currentlist.removeIndex(0);
		try {			
			PipeIn loader = new PipeIn();
			loader.loadContentFromBuffer(nextMod.readBytes(), PipeIn.LITTLEENDIAN);
			player = new S3MPlayer();
			player.load(loader);
			player.play();
//		player = ozm.getMOD(nextMod);
		} catch (Exception e) {
			e.printStackTrace();
			/* BAD MOD, REMOVE FROM LIST, TRY AGAIN */
			System.out.println("Error Loading Mod: "+nextMod.nameWithoutExtension());
			playlist.removeValue(nextMod, false);
			new Thread(nextSong).start();
			return;
		}
//		player.setMasterVolume(volume);
//		player.setDaemon(true);
//		player.setCallback(nextSong);
		player.setLoopable(false);
		player.play();
		if (nextMod.nameWithoutExtension().startsWith("musix-after")){
//			player.setMaxPlayTime(120);
		}
		setVolume(volume);
	}
	private void reloadActiveList() {
		currentlist.addAll(playlist);
		currentlist.shuffle();
		//always move "wild-perspective" to front of the list
		for (int ix=1; ix<currentlist.size; ix++) {
			if (currentlist.get(ix).name().contains("wild-perspective")) {
				currentlist.swap(0, ix);
				break;
			}
		}
	}

	public void setVolume(float volume) {
		if (player != null) {
//			player.setMasterVolume(volume);
		}
		this.volume=volume;
	}

	public void stop() {
		playlist.clear();
		currentlist.clear();
		if (player!=null) {
			player.done();
		}
	}
}
