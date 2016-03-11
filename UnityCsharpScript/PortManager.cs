using UnityEngine;
using System.IO.Ports;
using System.Threading;
using System.Collections;

public class PortManager : MonoBehaviour {

    private SerialPort serialPort = new SerialPort("COM3", 9600, Parity.None, 8);
    private Thread thread;
    public string line;
	// Use this for initialization
	void Start () {
        if (!serialPort.IsOpen)
        {
            serialPort.Open();
            serialPort.ReadTimeout = 1000000;
        }
        thread = new Thread(Read);
        thread.Start();
	
	}

    void OnDestroy()
    {
        Close();
    }

    public void Close()
    {
        if(thread != null && thread.IsAlive)
        {
            //thread.Join();
        }

        if(serialPort != null && serialPort.IsOpen)
        {
            serialPort.Close();
            serialPort.Dispose();
        }
    }

    private void Read()
    {
        Debug.Log("read!");
        while (serialPort != null && serialPort.IsOpen)
        {
            try
            {
                line = serialPort.ReadLine();
                
            }
            catch (System.Exception e)
            {
                Debug.LogWarning(e.Message);
            }
        }
        Debug.Log("readEnd");
    }

    // Update is called once per frame
    void Update () {
      

	}
}
