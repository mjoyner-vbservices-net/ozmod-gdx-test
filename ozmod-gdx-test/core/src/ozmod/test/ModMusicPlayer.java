package ozmod.test;

import ozmod.ITPlayer;
import ozmod.MODPlayer;
import ozmod.OZModPlayer;
import ozmod.OZModPlayer.IAudioDevice;
import ozmod.OZModRuntimeError;
import ozmod.S3MPlayer;
import ozmod.XMPlayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class ModMusicPlayer {
	OZModPlayer player;
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
	private float volume=1f;
	final private Array<FileHandle> playlist = new Array<FileHandle>();
	final private Array<FileHandle> currentlist = new Array<FileHandle>();

	public void pause(){
		if (player!=null && !Gdx.app.getType().equals(ApplicationType.Desktop)) {
			player.pause(true);
		}
	}
	
	public void resume(){
		if (player!=null && !Gdx.app.getType().equals(ApplicationType.Desktop)) {
			player.pause(false);
		}
	}
	
	public ModMusicPlayer() {
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
	
	public OZModPlayer getPlayerFor(IAudioDevice iaud, byte[] bytes) {
		OZModPlayer player;
		try {
			player = new ITPlayer(iaud);
			player.load(bytes);
			return player;
		} catch (OZModRuntimeError e) {
			e.printStackTrace();
		}
		try {
			player = new S3MPlayer(iaud);
			player.load(bytes);
			return player;
		} catch (OZModRuntimeError e) {
			e.printStackTrace();
		}
		try {
			player = new XMPlayer(iaud);
			player.load(bytes);
			return player;
		} catch (OZModRuntimeError e) {
			e.printStackTrace();
		}
		try {
			player = new MODPlayer(iaud);
			player.load(bytes);
			return player;
		} catch (OZModRuntimeError e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void play(float _volume) {
		final AudioDevice aud = Gdx.audio.newAudioDevice(44100, false);
		IAudioDevice iaud=new IAudioDevice() {
			@Override
			public void writeSamples(short[] samples, int offset, int numSamples) {
				aud.writeSamples(samples, offset, numSamples);					
			}
		};
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
			player = getPlayerFor(iaud, nextMod.readBytes());
			Gdx.app.log("ModMusicPlayer", "ModPlayer: "+player.getClass().getSimpleName());
			player.play();
		} catch (Exception e) {
			e.printStackTrace();
			/* BAD MOD, REMOVE FROM LIST, TRY AGAIN */
			System.out.println("Error Loading Mod: "+nextMod.nameWithoutExtension());
			playlist.removeValue(nextMod, false);
			new Thread(nextSong).start();
			return;
		}
		player.setVolume(volume);
		player.addWhenDone(nextSong);
		player.setLoopable(false);
		player.play();
		if (nextMod.nameWithoutExtension().startsWith("musix-after")){
			player.setMaxPlayTime(120);
		}
		setVolume(volume);
	}
	private void reloadActiveList() {
		currentlist.addAll(playlist);
		currentlist.shuffle();
		//always move "wild-perspective" to front of the list
//		for (int ix=1; ix<currentlist.size; ix++) {
//			if (currentlist.get(ix).name().contains("wild-perspective")) {
//				currentlist.swap(0, ix);
//				break;
//			}
//		}
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
