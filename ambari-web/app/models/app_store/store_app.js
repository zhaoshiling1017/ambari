/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var App = require('app');

App.StoreApp = DS.Model.extend({
  name: DS.attr('string'),
  title: DS.attr('string', {defaultValue: ''}),
  logoUrl: DS.attr('string'),
  description: DS.attr('string', {defaultValue: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam eget vehicula elit, ac sollicitudin enim. Curabitur ullamcorper vitae est at convallis. Curabitur et lacus vehicula, imperdiet sapien nec, tempus metus.\n\nPellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Etiam ut sapien leo. Quisque venenatis pellentesque erat, at auctor ipsum pulvinar ut.'}),
  services: DS.attr('string'),
  isActive: DS.attr('boolean'),
  isComponent: DS.attr('boolean'),
  storeCategories: DS.hasMany('App.StoreCategory'),
  storeCollections: DS.hasMany('App.StoreCollection'),
  configurations: DS.attr('array', {defaultValue:[{"name":Em.I18n.t('assemblies.app.deploy.popup.config.maxTransactions'),"minValue":100,"maxValue":100000,"step":100,"defaultValue":76000,"value":76000},{"name":Em.I18n.t('assemblies.app.deploy.popup.config.maxAnalysts'),"minValue":100,"maxValue":100000,"step":100,"defaultValue":300,"value":300}]}),

  /**
   * @type {boolean}
   */
  someConfigurationInvalid: function () {
    return this.get('configurations').someProperty('errorMessage');
  }.property('configurations.@each.errorMessage'),

  init: function () {
    this._super();
    var configurations = this.get('configurations');
    this.set('initConfigurations', JSON.parse(JSON.stringify(configurations)));
  }

});

App.StoreApp.FIXTURES = [
  {
    id: 1,
    name: 'Credit Fraud Detection',
    title: 'Credit Fraud Detection with Apache NiFi',
    logo_url: '/img/nifi-color.png',
    services: 'HBASE | KAFKA | STORM | ZOOKEEPER',
    is_active: false,
    is_component: false,
    description: 'Apache NiFi Eases Dataflow Management & Accelerates Time to Analytics in Banking.\n\nNiFi enables simple, fast data acquisition, secure data transport, prioritized data flow and clear traceability of data from the very edge of customer applications and endpoints all the way to the core data center.',
    configurations: [
      {
        name: Em.I18n.t('assemblies.app.deploy.popup.config.maxTransactions'),
        minValue: 100,
        maxValue: 100000,
        step: 100,
        defaultValue: 76000,
        value: 76000
      },
      {
        name: Em.I18n.t('assemblies.app.deploy.popup.config.maxAnalysts'),
        minValue: 100,
        maxValue: 100000,
        step: 100,
        defaultValue: 300,
        value: 300
      }
    ],
    store_categories: [4],
    store_collections: [1]
  },
  {
    id: 2,
    name: 'Log Search',
    title: 'Log Search with Apache Solr',
    logo_url: '/img/solr-color.png',
    services: 'ZOOKEEPER | LOG SEARCH',
    is_active: false,
    is_component: false,
    description: Em.I18n.t('assembly.manage.summary.logsearch.description') + "\n\nSolr is highly reliable, scalable and fault tolerant, providing distributed indexing, replication and load-balanced querying, automated failover and recovery, centralized configuration and more. Solr powers the search and navigation features of many of the world's largest internet sites.",
    store_categories: [6],
    store_collections: [],
    configurations: [
      {
        name: Em.I18n.t('assemblies.app.deploy.popup.logsearh.config.SourceComponents'),
        minValue: 1,
        maxValue: 10000,
        step: 100,
        defaultValue: 1000,
        value: 1000
      },
      {
        name: Em.I18n.t('assemblies.app.deploy.popup.logsearch.config.logEvents'),
        minValue: 100,
        maxValue: 300000,
        step: 100,
        defaultValue: 150000,
        value: 150000
      }
    ]
  },
  {
    id: 3,
    name: 'Apache Metron',
    logo_url: '/img/metron-color.png',
    services: 'HBASE | ACCUMULO | ZOOKEEPER',
    is_active: false,
    is_component: false,
    description: 'Apache Metron is the next evolution of Security Incident Event Management.\nMetron helps users process unprecedented volumes of data per second, changing the game for malware detection and prevention. When an organization is attacked, Metron users can process and compare data from comprehensive feeds across the platform in real time. This not only facilitates enhanced detection of malware campaigns, but also impacts the economics for attackers by requiring them to customize malware for each target. ',
    store_categories: [4],
    store_collections: [1]
  },
  {
    id: 4,
    name: 'ETL in 1 Hour',
    logo_url: '/img/sqoop-color.png',
    services: 'SQOOP | OOZIE',
    is_active: false,
    is_component: false,
    description: 'Sqoop is a command-line interface application for transferring data between relational databases and Hadoop.\n It supports incremental loads of a single table or a free form SQL query as well as saved jobs which can be run multiple times to import updates made to a database since the last import. Imports can also be used to populate tables in Hive or HBase.',
    store_categories: [3],
    store_collections: []
  },
  {
    id: 5,
    name: 'Backups',
    logo_url: '/img/falcon-color.png',
    services: 'KAFKA | STORM | ZOOKEEPER',
    is_active: false,
    is_component: false,
    store_categories: [3],
    store_collections: []
  },
  {
    id: 6,
    name: 'Prediction',
    logo_url: '/img/spark-color.png',
    services: 'HBASE | ACCUMULO | ZOOKEEPER',
    is_active: false,
    is_component: false,
    store_categories: [6],
    store_collections: []
  },
  {
    id: 7,
    name: 'Broker Mgmt',
    logo_url: '/img/metron-color.png',
    services: 'HBASE | ACCUMULO | ZOOKEEPER',
    is_active: false,
    is_component: false,
    store_categories: [7],
    store_collections: []
  },
  {
    id: 8,
    name: 'Configuration',
    logo_url: '/img/storm-color.png',
    services: 'HBASE | ACCUMULO | ZOOKEEPER',
    is_active: false,
    is_component: false,
    store_categories: [3],
    store_collections: []
  },
  {
    id: 9,
    name: 'Caching',
    logo_url: '/img/memcache-color.png',
    services: 'HBASE | ACCUMULO | ZOOKEEPER',
    is_active: false,
    is_component: false,
    store_categories: [4],
    store_collections: []
  },
  {
    id: 10,
    name: 'Fast Retrieval',
    logo_url: '/img/accumulo-color.png',
    services: 'HBASE | ACCUMULO | ZOOKEEPER',
    is_active: false,
    is_component: false,
    store_categories: [6],
    store_collections: []
  },
  {
    id: 11,
    name: 'Apache HBase',
    logo_url: '/img/hbase-color.png',
    is_active: false,
    services: [],
    is_component: true,
    description: 'Apache HBase is an open source, non-relational, distributed database. \nHBase features compression, in-memory operation, and Bloom filters on a per-column basis as outlined in the original BigTable paper. Tables in HBase can serve as the input and output for MapReduce jobs run in Hadoop, and may be accessed through the Java API but also through REST, Avro or Thrift gateway APIs. ',
    store_categories: [7],
    store_collections: [1]
  },
  {
    id: 12,
    name: 'Apache Storm',
    logo_url: '/img/storm-color.png',
    is_active: false,
    services: [],
    is_component: true,
    description: 'Apache Storm is a distributed computation framework written predominantly in the Clojure programming language.\n Originally created by Nathan Marz and team at BackType, the project was open sourced after being acquired by Twitter. It uses custom created "spouts" and "bolts" to define information sources and manipulations to allow batch, distributed processing of streaming data.',
    store_categories: [3],
    store_collections: [1]
  },
  {
    id: 13,
    name: 'Jenkins',
    logo_url: '/img/jenkins-color.png',
    is_active: false,
    services: [],
    is_component: true,
    description: 'Jenkins is an open source continuous integration tool written in Java.\nJenkins provides continuous integration services for software development. It is a server-based system running in a servlet container such as Apache Tomcat. It supports SCM tools including AccuRev, CVS, Subversion, Git, Mercurial, Perforce, Clearcase and RTC, and can execute Apache Ant and Apache Maven based projects as well as arbitrary shell scripts and Windows batch commands.',
    store_categories: [6],
    store_collections: []
  },
  {
    id: 14,
    name: 'Apache Kafka',
    logo_url: '/img/kafka-color.png',
    is_active: false,
    services: [],
    is_component: true,
    description: 'Apache Kafka is an open-source message broker project developed by the Apache Software Foundation written in Scala.\n The project aims to provide a unified, high-throughput, low-latency platform for handling real-time data feeds. It is, in its essence, a "massively scalable pub/sub message queue architected as a distributed transaction log", making it highly valuable for enterprise infrastructures.',
    store_categories: [7],
    store_collections: [1]
  },
  {
    id: 15,
    name: 'Apache Spark',
    logo_url: '/img/spark-color.png',
    is_active: false,
    services: [],
    is_component: true,
    description: 'Apache Spark provides programmers with an application programming interface centered on a data structure called the resilient distributed dataset (RDD), a read-only multiset of data items distributed over a cluster of machines, that is maintained in a fault-tolerant way.\n It was developed in response to limitations in the MapReduce cluster computing paradigm, which forces a particular linear dataflow structure on distributed programs.',
    store_categories: [6],
    store_collections: []
  },
  {
    id: 16,
    name: 'Apache Solr',
    logo_url: '/img/solr-color.png',
    is_active: false,
    services: [],
    is_component: true,
    description: 'Solr (pronounced "solar") is an open source enterprise search platform, written in Java, from the Apache Lucene project.\n Its major features include full-text search, hit highlighting, faceted search, real-time indexing, dynamic clustering, database integration, NoSQL features and rich document (e.g., Word, PDF) handling.',
    store_categories: [6],
    store_collections: []
  },
  {
    id: 17,
    name: 'ZooKeeper',
    logo_url: '/img/zookeeper-color.png',
    is_active: false,
    services: [],
    is_component: true,
    description: 'ZooKeeper is a centralized service for maintaining configuration information, naming, providing distributed synchronization, and providing group services.\n All of these kinds of services are used in some form or another by distributed applications. Each time they are implemented there is a lot of work that goes into fixing the bugs and race conditions that are inevitable.',
    store_categories: [6],
    store_collections: []
  }
];