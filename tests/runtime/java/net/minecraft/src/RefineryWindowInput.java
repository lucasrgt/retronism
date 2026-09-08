package net.minecraft.src;

import java.io.*;
import java.lang.reflect.*;
import java.security.MessageDigest;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;

/** Test-only Windows messages enter LWJGL's native window procedure and input queues. */
public final class RefineryWindowInput {
    private final long hwnd;
    private final Method send;
    private final File log;
    public RefineryWindowInput(File root) throws Exception {
        log=new File(root,"window-input.log");
        Class<?> windows=Class.forName("org.lwjgl.opengl.WindowsDisplay");
        Field implementation=Display.class.getDeclaredField("display_impl"); implementation.setAccessible(true);
        Object display=implementation.get(null);
        RefineryFixture.check(windows.isInstance(display),"native input proof requires Windows LWJGL");
        Field handle=windows.getDeclaredField("hwnd"); handle.setAccessible(true); hwnd=handle.getLong(display);
        RefineryFixture.check(hwnd!=0,"input target is this client's own native window");
        send=windows.getDeclaredMethod("sendMessage",long.class,long.class,long.class,long.class);
        send.setAccessible(true);
        File jar=new File(Display.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        MessageDigest hash=MessageDigest.getInstance("SHA-256");
        try(InputStream input=new FileInputStream(jar)) {
            byte[] data=new byte[8192]; int count;
            while((count=input.read(data))!=-1) hash.update(data,0,count);
        }
        StringBuilder value=new StringBuilder();
        for(byte b:hash.digest()) value.append(String.format("%02x",b&255));
        RefineryFixture.check(value.toString().equals("833e721817f70d1445eec13d8ce5a86e12af70efac5014356302e2bbc68b3fe2"),"pinned LWJGL input ABI");
        record("window="+hwnd+" lwjglSha256="+value);
    }
    public void focus(Minecraft client) throws Exception {
        message(7,0,0); // WM_SETFOCUS; scoped to this game window.
        client.displayGuiScreen(null); client.setIngameFocus();
        RefineryFixture.check(client.inGameHasFocus && client.currentScreen==null,"game accepts mouse input");
    }
    public void selectWrench(int facing,int tick) throws Exception {
        // Native '2' key press/release selects hotbar index 1 through Minecraft's keyboard loop.
        message(0x100,0x32,0x00030001L);
        message(0x101,0x32,0xc0030001L);
        record("facing="+facing+" tick="+tick+" key=2 down+up");
    }
    public void rightClick(Minecraft client,int facing,int tick,String expected) throws Exception {
        long position=((long)(client.displayHeight/2)<<16)|(client.displayWidth/2);
        message(0x204,2,position); // WM_RBUTTONDOWN -> WindowsMouse -> Mouse.next -> clickMouse(1).
        message(0x205,0,position); // WM_RBUTTONUP; never leave a held button behind.
        record("facing="+facing+" tick="+tick+" button=right down+up expected="+expected);
    }
    private void message(long id,long wparam,long lparam) throws Exception { send.invoke(null,hwnd,id,wparam,lparam); }
    private void record(String line) throws IOException {
        try(PrintWriter output=new PrintWriter(new FileWriter(log,true))) { output.println(line); }
    }
}
