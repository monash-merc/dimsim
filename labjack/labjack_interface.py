import ctypes, time

class Labjack(object):
    def __init__(self, lib='/usr/local/lib/liblabjack.so'):
        self.liblabjack=ctypes.CDLL(lib)
        self.idnum = ctypes.c_long(-1)
        self.liblabjack.InitLabjack()
        self.version = self.liblabjack.GetFirmwareVersion(ctypes.byref(self.idnum))
        print 'labjack (%d) firmware version: %f' % (self.idnum.value, self.version)

    def take_sample(self):
        tempC = ctypes.c_float(0)
        tempF = ctypes.c_float(0)
        rh = ctypes.c_float(0)
        err = self.liblabjack.SHT1X(ctypes.byref(self.idnum), ctypes.c_long(0), ctypes.c_long(0), ctypes.c_long(0), ctypes.c_long(0), ctypes.byref(tempC), ctypes.byref(tempF), ctypes.byref(rh))
        if err != 0:
            print 'error occured: %d' % err
        return (tempC.value, tempF.value, rh.value)


