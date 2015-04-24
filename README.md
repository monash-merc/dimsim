Distributed Instrument and Multi-Sensor Integrated Middleware (DIMSIM)
GENERAL INFORMATION
1.1 Background

Australian Federal Government's National Collaborative Research Infrastructure Strategy (NCRIS) Roadmap for 2006 recognizes e-research and availability of platforms for collaboration as vital tools for Australian researchers. According to NCRIS: seamless access enables researchers to carry out their research more creatively, efficiently and collaboratively across long distances, regardless of location and time, and disseminate their research outcomes with greater effect.

A core requirement for many of the NCRIS capabilities is the ability to capture data coming from a range of instruments. Current and future user requirements in area of scientific instrument data capture can be summarized as below:

    Instrument monitoring in real-time (state plus images)
    Instrument programmatic control (will vary by instrument type and owner requirements)
    Data acquisition
    Ability to add new instruments to system
    Secure instrument management/control/acquisition 

Archer project's Distributed Instrument and Multi-Sensor Integrated Middleware (DIMSIM) module aims to identify infrastructure requirements and develop software solutions to the above user requirements.
1.2 DIMSIM overview

Dimsim leverages existing work in the area of data collection and monitoring. In particular

    CIMA : JCU (Ian Atkinson's group) and Indiana (Ric McMullen?'s group) Universities
    JAINIS work at JCU as part of the DART project
    Instrument management work at University of Sydney by Peter Turner's group 

1.2.1 Common Instrument Middleware Architecture (CIMA)

Research instruments vary widely in their design, construction, and interfaces. The Common Instrument Middleware Architecture was designed to provide a single virtualisation layer to hide this complexity, and offer a relatively simple Web service interface to the rest of the data pipeline. CIMA was initially proposed and used by Ric McMullen?'s group at the Indiana University.

CIMA framework is focused primarily on providing a virtualisation layer to capture data from a variety of instruments. Key features of CIMA include

    linking network instruments with remote data store
    exchange messages using XML parcels
    subscription based, clients request data from producers
    key components : Instrument Representative, Data Manager & Plugins 

1.2.2 JAINIS

Ian Atkinson's group at James Cook University recognized that adapting CIMA to the Australian context, in particular to reef sensor grids and X-ray crystallography labs at leading universities in Australia will further NCRIS goal to develop platforms for collaboration. JAINIS extensions to CIMA developed as part of DART project provided reliable data transfer and verification using Kepler workflows and Gridsphere portal.
1.2.3 CIMA redesign (JCU & USyd)

A review of CIMA software components carried out as part of the Archer project identified the following:

    CIMA code base was tightly coupled to client infrastructure. (i.e., the x-ray crystallography environment.)
    It was not possible to implement CIMA as an independent, out-of-the-box solution to external sites without extensive modifications and rewrite. 

To address the above problems, Peter Turner's group, Usyd and Archer (Ian Atkinson's group, JCU) decided to re-invent CIMA by redesigning the code base. In particular, all linkages to client infrastructure was removed and CIMA was restricted to implementaiton of a message delivery system defined by an agreed upon schema for messages termed 'parcels' 
