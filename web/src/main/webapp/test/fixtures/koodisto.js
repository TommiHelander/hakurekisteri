function koodistoFixtures() {
    var httpBackend = testFrame().httpBackend
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/kieli\/koodi$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/luokkataso\/koodi$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/yksilollistaminen\/koodi$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/suorituksentila\/koodi$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/arvosanat\/koodi$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/kielivalikoima\/koodi$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/aidinkielijakirjallisuus\/koodi$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/oppiaineetyleissivistava\/koodi$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_a1$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_a12$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_a2$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_a22$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_ai$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_b1$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_b2$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_b22$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_b23$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_b3$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_b32$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_b33$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_bi$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_fi$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_fy$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_ge$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_hi$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_ke$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_ko$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_ks$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_kt$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_ku$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_li$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_ma$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_mu$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_ps$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_te$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/relaatio\/sisaltyy-alakoodit\/oppiaineetyleissivistava_yh$/).passThrough()
    httpBackend.when('GET', /.*koodisto-service\/rest\/json\/koulutus\/koodi\/koulutus_671116/).passThrough()
}