def tyt_puan_hesapla(tur, mat, sos, fen, obp):
    """TYT Ham ve Yerleştirme Puanını hesaplar."""
    ham = 100 + (tur * 3.3 + mat * 3.3 + sos * 3.4 + fen * 3.4)
    yerlestirme = ham + (obp * 0.6)
    return ham, yerlestirme

def ayt_say_puan_hesapla(tyt_ham, mat, fiz, kim, biyo, obp):
    """AYT Sayısal Ham ve Yerleştirme Puanını hesaplar."""
    base = 100 + (tyt_ham * 0.4)
    ham = base + (mat * 3.0 + fiz * 2.85 + kim * 3.07 + biyo * 3.07)
    yerlestirme = ham + (obp * 0.6)
    return ham, yerlestirme

def ayt_ea_puan_hesapla(tyt_ham, mat, edb, tar1, cog1, obp):
    """AYT Eşit Ağırlık Ham ve Yerleştirme Puanını hesaplar."""
    base = 100 + (tyt_ham * 0.4)
    ham = base + (mat * 3.0 + edb * 3.0 + tar1 * 2.8 + cog1 * 3.33)
    yerlestirme = ham + (obp * 0.6)
    return ham, yerlestirme

def ayt_soz_puan_hesapla(tyt_ham, edb, tar1, cog1, tar2, cog2, fel, din, obp):
    """AYT Sözel Ham ve Yerleştirme Puanını hesaplar."""
    base = 100 + (tyt_ham * 0.4)
    ham = base + (edb * 3.0 + tar1 * 2.8 + cog1 * 3.33 + tar2 * 2.91 + cog2 * 2.91 + fel * 3.0 + din * 3.33)
    yerlestirme = ham + (obp * 0.6)
    return ham, yerlestirme
